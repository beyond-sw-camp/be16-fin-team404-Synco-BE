package com.team404.synco.drive.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team404.synco.drive.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProjectDocumentRedisService implements MessageListener {

    private final ObjectMapper objectMapper;
    private final SimpMessageSendingOperations messagingTemplate;
    private final RedisTemplate<String, String> documentPubSubTemplate;
    private final RedisTemplate<String, String> documentOnlineUsersTemplate;
    private final RedisTemplate<String, Object> workspaceMembersTemplate;

    // Redis 키 패턴
    private static final String ONLINE_USERS_KEY = "document:online-users:";
    private static final String LINE_LOCKS_KEY = "document:locks:"; // 라인 락 키
    // 워크스페이스 멤버 확인용 (Redis DB 2)
    private static final String WORKSPACE_KEY_PREFIX = "workSpaceSeq:";
    private static final String FRIEND_LIST = "friendList";

    // TTL 설정 (안전장치: 비정상 종료 시 자동 정리)
    private static final int ONLINE_USERS_TTL_MINUTES = 30;
    private static final int LINE_LOCK_TTL_SECONDS = 30; // 라인 락 TTL (30초로 증가)
    // STOMP Topics
    private static final String TOPIC_PREFIX = "/topic/document/";
    private static final String SUFFIX_DOCUMENT_UPDATE = "/document-update";
    private static final String SUFFIX_ONLINE_USERS = "/online-users";
    private static final String SUFFIX_LINE_LOCKS = "/line-locks"; // 라인 락 토픽

    public ProjectDocumentRedisService(
            ObjectMapper objectMapper,
            SimpMessageSendingOperations messagingTemplate,
            @Qualifier("documentPubSubTemplate") RedisTemplate<String, String> documentPubSubTemplate,
            @Qualifier("documentOnlineUsers") RedisTemplate<String, String> documentOnlineUsersTemplate,
            @Qualifier("workspaceMembers") RedisTemplate<String, Object> workspaceMembersTemplate) {
        this.objectMapper = objectMapper;
        this.messagingTemplate = messagingTemplate;
        this.documentPubSubTemplate = documentPubSubTemplate;
        this.documentOnlineUsersTemplate = documentOnlineUsersTemplate;
        this.workspaceMembersTemplate = workspaceMembersTemplate;
    }

    // ==================== Redis Pub/Sub 메시지 발행 및 수신 ====================

    /**
     * 문서 업데이트 메시지를 Redis로 발행
     */
    public void publishDocumentUpdateToRedis(EditorMessageDto messageDto) {
        try {
            // Redis Pub/Sub으로 다른 서버들에게 브로드캐스트
            String channel = TOPIC_PREFIX + messageDto.getDocumentId() + SUFFIX_DOCUMENT_UPDATE;
            String message = objectMapper.writeValueAsString(messageDto);
            documentPubSubTemplate.convertAndSend(channel, message);

        } catch (Exception e) {
            log.error("❌ 문서 업데이트 발행 실패 - DocumentId: {}, ResponseDto: {}",
                    messageDto.getDocumentId(), messageDto, e);
        }
    }

    /**
     * 사용자 접속 처리
     */
    public void publishUserJoinToRedis(Long documentId, UserJoinLeaveDto joinDto) {
        log.info("👤 사용자 접속 - DocumentId: {}, UserId: {}, UserName: {}",
            documentId, joinDto.getUserId(), joinDto.getUserName());

        try {
            // 1. Redis에 온라인 사용자 추가 (중복 체크)
            String onlineKey = ONLINE_USERS_KEY + documentId;
            String userIdStr = joinDto.getUserId().toString();
            
            // 이미 해당 사용자가 온라인인지 확인
            if (documentOnlineUsersTemplate.opsForHash().hasKey(onlineKey, userIdStr)) {
                log.info("⚠️ 사용자 이미 온라인 - DocumentId: {}, UserId: {}", documentId, joinDto.getUserId());
                return; // 이미 온라인이면 추가하지 않음
            }
            
            // 새로운 사용자만 추가
            documentOnlineUsersTemplate.opsForHash().put(
                onlineKey,
                userIdStr,
                joinDto.getUserName()
            );
            // TTL 설정 (비정상 종료 대비)
            documentOnlineUsersTemplate.expire(onlineKey, ONLINE_USERS_TTL_MINUTES, TimeUnit.MINUTES);

            // 2. USER_JOIN 메시지 생성 (간단하게)
            Map<String, Object> joinMessage = new HashMap<>();
            joinMessage.put("messageType", "USER_JOIN");
            joinMessage.put("userId", joinDto.getUserId());
            joinMessage.put("userName", joinDto.getUserName());

            // 3. Redis Pub/Sub으로 다른 서버들에게 브로드캐스트
            String channel = TOPIC_PREFIX + documentId + SUFFIX_ONLINE_USERS;
            String message = objectMapper.writeValueAsString(joinMessage);
            documentPubSubTemplate.convertAndSend(channel, message);

        } catch (Exception e) {
            log.error("❌ 사용자 접속 처리 실패 - DocumentId: {}, UserId: {}",
                documentId, joinDto.getUserId(), e);
        }
    }

    /**
     * 사용자 이탈 처리
     */
    public void publishUserLeaveToRedis(Long documentId, UserJoinLeaveDto leaveDto) {
        log.info("👋 사용자 이탈 - DocumentId: {}, UserId: {}, UserName: {}",
            documentId, leaveDto.getUserId(), leaveDto.getUserName());

        try {
            // 1. Redis에서 온라인 사용자 제거
            String onlineKey = ONLINE_USERS_KEY + documentId;
            documentOnlineUsersTemplate.opsForHash().delete(onlineKey, leaveDto.getUserId().toString());

            // 2. USER_LEAVE 메시지 생성 (간단하게)
            Map<String, Object> leaveMessage = new HashMap<>();
            leaveMessage.put("messageType", "USER_LEAVE");
            leaveMessage.put("userId", leaveDto.getUserId());
            leaveMessage.put("userName", leaveDto.getUserName());

            // 3. Redis Pub/Sub으로 다른 서버들에게 브로드캐스트
            String channel = TOPIC_PREFIX + documentId + SUFFIX_ONLINE_USERS;
            String message = objectMapper.writeValueAsString(leaveMessage);
            documentPubSubTemplate.convertAndSend(channel, message);

        } catch (Exception e) {
            log.error("❌ 사용자 이탈 처리 실패 - DocumentId: {}, UserId: {}",
                documentId, leaveDto.getUserId(), e);
        }
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        
        // Redis Keyspace Notification (키 만료 이벤트)
        if (channel.contains("__keyevent@") && channel.endsWith(":expired")) {
            handleKeyExpiration(body);
            return;
        }
        
        // 일반 Redis Pub/Sub 메시지
        String dest = normalizeChannel(channel);

        if (channel.contains(SUFFIX_DOCUMENT_UPDATE)) {
            handleLineUpdateMessage(dest, body);
        } else if (channel.contains(SUFFIX_ONLINE_USERS)) {
            handleOnlineUsersMessage(dest, body);
        } else if (channel.contains(SUFFIX_LINE_LOCKS)) {
            handleLineLockMessage(dest, body);
        } else {
            log.warn("⚠️ 알 수 없는 채널 메시지 수신 - Channel: {}", channel);
        }
    }

    // ==================== Redis Pub/Sub 메시지 핸들러들 ====================

    private void handleLineUpdateMessage(String dest, String body) {
        try {
            // Redis에서 받은 라인별 업데이트를 STOMP 클라이언트들에게 전달
            messagingTemplate.convertAndSend(dest, body);
            log.debug("✅ 라인별 업데이트 브로드캐스트 완료 - Dest: {}", dest);
        } catch (Exception e) {
            log.error("❌ 라인별 업데이트 메시지 처리 실패", e);
        }
    }

    private void handleOnlineUsersMessage(String dest, String body) {
        try {
            // Redis에서 받은 온라인 사용자 목록을 STOMP 클라이언트들에게 전달
            messagingTemplate.convertAndSend(dest, body);
            log.debug("✅ 온라인 사용자 목록 브로드캐스트 완료 - Dest: {}", dest);
        } catch (Exception e) {
            log.error("❌ 온라인 사용자 메시지 처리 실패", e);
        }
    }

    private void handleLineLockMessage(String dest, String body) {
        try {
            // Redis에서 받은 라인 락 정보를 STOMP 클라이언트들에게 전달
            messagingTemplate.convertAndSend(dest, body);
            log.debug("✅ 라인 락 정보 브로드캐스트 완료 - Dest: {}", dest);
        } catch (Exception e) {
            log.error("❌ 라인 락 메시지 처리 실패", e);
        }
    }

    /**
     * Redis Key 만료 이벤트 처리 (TTL 자동 해제)
     */
    private void handleKeyExpiration(String expiredKey) {
        log.info("⏰ Redis Key 만료 이벤트 수신: {}", expiredKey);

        // "document:locks:documentId:lineId" 패턴 파싱
        if (expiredKey.startsWith("document:locks:")) {
            String[] parts = expiredKey.split(":");
            if (parts.length == 4) {
                String documentId = parts[2];
                String lineId = parts[3];

                log.info("🔓 자동 UNLOCK 브로드캐스트 - DocumentId: {}, LineId: {}", documentId, lineId);

                try {
                    // UNLOCK 메시지 생성 (senderId: SYSTEM)
                    EditorMessageDto unlockMessage = EditorMessageDto.builder()
                        .messageType(EditorMessageDto.MessageType.UNLOCK)
                        .documentId(documentId)
                        .lineId(lineId)
                        .senderId("SYSTEM")
                        .build();

                    // Redis Pub/Sub으로 브로드캐스트
                    String channel = TOPIC_PREFIX + documentId + SUFFIX_LINE_LOCKS;
                    String messageBody = objectMapper.writeValueAsString(unlockMessage);
                    publishToLineLockChannel(channel, messageBody);

                    log.info("✅ 자동 락 해제 브로드캐스트 완료 - LineId: {}", lineId);

                } catch (Exception e) {
                    log.error("❌ 락 만료 처리 실패 - DocumentId: {}, LineId: {}", documentId, lineId, e);
                }
            } else {
                log.warn("⚠️ 예상치 못한 Redis 락 키 형식: {}", expiredKey);
            }
        }
    }

    // ==================== Redis 데이터 조회 메서드들 ====================

    /**
     * 참여자 목록 조회 (DTO 형태)
     */
    public ParticipantsResponseDto getDocumentParticipants(Long documentId) {
        String onlineKey = ONLINE_USERS_KEY + documentId;
        Map<Object, Object> rawEntries = documentOnlineUsersTemplate.opsForHash().entries(onlineKey);

        List<ParticipantDto> participants = rawEntries.entrySet().stream()
            .map(entry -> ParticipantDto.builder()
                .userId(Long.parseLong(entry.getKey().toString()))
                .userName(entry.getValue().toString())
                .build())
            .collect(Collectors.toList());

        return ParticipantsResponseDto.builder()
            .participants(participants)
            .build();
    }

    // ==================== 라인 락 관리 ====================

    /**
     * 라인 잠금 처리
     */
    public void publishLineLockToRedis(EditorMessageDto lockDto) {
        log.info("🔒 라인 잠금 - DocumentId: {}, LineId: {}, UserId: {}, UserName: {}",
            lockDto.getDocumentId(), lockDto.getLineId(), lockDto.getUserId(), lockDto.getUserName());

        try {
            // 1. Redis에 락 정보 저장
            String lockKey = LINE_LOCKS_KEY + lockDto.getDocumentId() + ":" + lockDto.getLineId();
            Map<String, String> lockInfo = new HashMap<>();
            lockInfo.put("userId", lockDto.getUserId().toString());
            lockInfo.put("userName", lockDto.getUserName());
            lockInfo.put("timestamp", String.valueOf(System.currentTimeMillis()));

            String lockValue = objectMapper.writeValueAsString(lockInfo);
            documentOnlineUsersTemplate.opsForValue().set(lockKey, lockValue, LINE_LOCK_TTL_SECONDS, TimeUnit.SECONDS);

            // 2. Redis Pub/Sub으로 다른 서버들에게 브로드캐스트
            String channel = TOPIC_PREFIX + lockDto.getDocumentId() + SUFFIX_LINE_LOCKS;
            String message = objectMapper.writeValueAsString(lockDto);
            documentPubSubTemplate.convertAndSend(channel, message);

            log.debug("✅ 라인 잠금 완료 - LineId: {}, TTL: {}초", lockDto.getLineId(), LINE_LOCK_TTL_SECONDS);

        } catch (Exception e) {
            log.error("❌ 라인 잠금 처리 실패 - DocumentId: {}, LineId: {}",
                lockDto.getDocumentId(), lockDto.getLineId(), e);
        }
    }

    /**
     * 라인 잠금 해제 처리
     */
    public void publishLineUnlockToRedis(EditorMessageDto unlockDto) {
        log.info("🔓 라인 잠금 해제 - DocumentId: {}, LineId: {}, UserId: {}",
            unlockDto.getDocumentId(), unlockDto.getLineId(), unlockDto.getUserId());

        try {
            // 1. Redis에서 락 정보 확인 및 삭제
            String lockKey = LINE_LOCKS_KEY + unlockDto.getDocumentId() + ":" + unlockDto.getLineId();
            String lockValue = documentOnlineUsersTemplate.opsForValue().get(lockKey);

            if (lockValue != null) {
                // 락을 건 사용자만 해제 가능하도록 확인
                Map<String, String> lockInfo = objectMapper.readValue(lockValue, new TypeReference<Map<String, String>>() {});
                String lockedUserId = lockInfo.get("userId");

                if (lockedUserId.equals(unlockDto.getUserId().toString())) {
                    documentOnlineUsersTemplate.delete(lockKey);
                    log.debug("✅ 라인 잠금 해제 완료 - LineId: {}", unlockDto.getLineId());
                } else {
                    log.warn("⚠️ 라인 잠금 해제 권한 없음 - LineId: {}, RequestUserId: {}, LockedUserId: {}",
                        unlockDto.getLineId(), unlockDto.getUserId(), lockedUserId);
                    return; // 권한 없으면 브로드캐스트 안 함
                }
            }

            // 2. Redis Pub/Sub으로 다른 서버들에게 브로드캐스트
            String channel = TOPIC_PREFIX + unlockDto.getDocumentId() + SUFFIX_LINE_LOCKS;
            String message = objectMapper.writeValueAsString(unlockDto);
            documentPubSubTemplate.convertAndSend(channel, message);

        } catch (Exception e) {
            log.error("❌ 라인 잠금 해제 실패 - DocumentId: {}, LineId: {}",
                unlockDto.getDocumentId(), unlockDto.getLineId(), e);
        }
    }

    /**
     * 문서의 모든 라인 락 정보 조회 (DTO 형태)
     */
    public LineLocksResponseDto getAllLineLocksAsDto(Long documentId) {
        try {
            String lockPattern = LINE_LOCKS_KEY + documentId + ":*";
            Set<String> lockKeys = documentOnlineUsersTemplate.keys(lockPattern);

            List<LineLockDto> locks = new ArrayList<>();
            if (lockKeys != null) {
                for (String key : lockKeys) {
                    String lineId = key.substring((LINE_LOCKS_KEY + documentId + ":").length());
                    String lockValue = documentOnlineUsersTemplate.opsForValue().get(key);
                    if (lockValue != null) {
                        Map<String, String> lockInfo = objectMapper.readValue(lockValue, new TypeReference<Map<String, String>>() {});
                        locks.add(LineLockDto.builder()
                            .lineId(lineId)
                            .userId(Long.parseLong(lockInfo.get("userId")))
                            .userName(lockInfo.get("userName"))
                            .timestamp(Long.parseLong(lockInfo.get("timestamp")))
                            .build());
                    }
                }
            }

            return LineLocksResponseDto.builder()
                .documentId(documentId)
                .locks(locks)
                .build();

        } catch (Exception e) {
            log.error("❌ 라인 락 목록 조회 실패 - DocumentId: {}", documentId, e);
            return LineLocksResponseDto.builder()
                .documentId(documentId)
                .locks(new ArrayList<>())
                .build();
        }
    }

    // ==================== 워크스페이스 멤버 확인 (Redis DB 2) ====================


    // 워크스페이스 멤버 여부 확인
    public boolean isMemberOfWorkspace(Long workspaceSeq, Long userSeq) {
        try {
            String workspaceKey = WORKSPACE_KEY_PREFIX + workspaceSeq;

            // Redis에서 friendList 조회
            Object existing = workspaceMembersTemplate.opsForHash().get(workspaceKey, FRIEND_LIST);

            if (existing == null) {
                log.warn("⚠️ 워크스페이스 멤버 목록 없음 - WorkspaceSeq: {}", workspaceSeq);
                return false;
            }

            // JSON 파싱
            List<Long> memberList = objectMapper.readValue(
                    existing.toString(),
                    new TypeReference<List<Long>>() {}
            );

            return memberList.contains(userSeq);

        } catch (Exception e) {
            return false;
        }
    }

    // ==================== 유틸리티 메서드들 ====================

    /**
     * KeyExpirationListener에서 호출하는 메서드
     * 라인 락 채널로 직접 브로드캐스트
     */
    public void publishToLineLockChannel(String channel, String message) {
        try {
            messagingTemplate.convertAndSend(channel, message);
            log.debug("✅ 라인 락 채널 브로드캐스트 완료 - Channel: {}", channel);
        } catch (Exception e) {
            log.error("❌ 라인 락 채널 브로드캐스트 실패 - Channel: {}", channel, e);
        }
    }

    // 채널 문자열 정규화 (끝에 '/' 제거)
    private static String normalizeChannel(String channel) {
        if (channel == null || channel.isEmpty()) {
            return channel;
        }
        int len = channel.length();
        return (len > 1 && channel.charAt(len - 1) == '/')
                ? channel.substring(0, len - 1)
                : channel;
    }
}
