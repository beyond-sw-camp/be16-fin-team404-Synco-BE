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

/**
 * YJS 기반 실시간 문서 편집 Redis 서비스
 * 
 * 역할:
 * 1. Redis Pub/Sub을 통한 서버 간 메시지 브로드캐스트
 * 2. 온라인 사용자 관리 (Redis Hash)
 * 3. 커서 위치 관리 (Redis Hash)
 * 4. 워크스페이스 멤버 확인 (Redis DB 2 조회)
 */
@Service
@Slf4j
public class ProjectDocumentRedisService implements MessageListener {

    private final ObjectMapper objectMapper;
    private final SimpMessageSendingOperations messagingTemplate;
    private final DocumentSyncService documentSyncService;
    private final RedisTemplate<String, String> documentPubSubTemplate;
    private final RedisTemplate<String, String> documentOnlineUsersTemplate;
    private final RedisTemplate<String, String> documentCursorsTemplate;
    private final RedisTemplate<String, Object> workspaceMembersTemplate;

    // Redis 키 패턴
    private static final String ONLINE_USERS_KEY = "document:online-users:";
    private static final String CURSORS_KEY = "document:cursors:";
    
    // 워크스페이스 멤버 확인용 (Redis DB 2)
    private static final String WORKSPACE_KEY_PREFIX = "workSpaceSeq:";
    private static final String FRIEND_LIST = "friendList";
    
    // TTL 설정 (안전장치: 비정상 종료 시 자동 정리)
    private static final int ONLINE_USERS_TTL_MINUTES = 30;
    private static final int CURSORS_TTL_MINUTES = 30;
    
    // STOMP Topics
    private static final String TOPIC_PREFIX = "/topic/document/";
    private static final String SUFFIX_YJS_UPDATE = "/yjs-update";
    private static final String SUFFIX_CURSOR = "/cursor";
    private static final String SUFFIX_ONLINE_USERS = "/online-users";

    public ProjectDocumentRedisService(
            ObjectMapper objectMapper,
            SimpMessageSendingOperations messagingTemplate,
            DocumentSyncService documentSyncService,
            @Qualifier("documentPubSubTemplate") RedisTemplate<String, String> documentPubSubTemplate,
            @Qualifier("documentOnlineUsers") RedisTemplate<String, String> documentOnlineUsersTemplate,
            @Qualifier("documentCursors") RedisTemplate<String, String> documentCursorsTemplate,
            @Qualifier("workspaceMembers") RedisTemplate<String, Object> workspaceMembersTemplate) {
        this.objectMapper = objectMapper;
        this.messagingTemplate = messagingTemplate;
        this.documentSyncService = documentSyncService;
        this.documentPubSubTemplate = documentPubSubTemplate;
        this.documentOnlineUsersTemplate = documentOnlineUsersTemplate;
        this.documentCursorsTemplate = documentCursorsTemplate;
        this.workspaceMembersTemplate = workspaceMembersTemplate;
    }

    // ==================== Redis Pub/Sub 메시지 수신 (서버 간 동기화) ====================
    
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        String dest = normalizeChannel(channel);
        
        try {
            if (dest.endsWith(SUFFIX_YJS_UPDATE)) {
                handleYjsUpdateMessage(dest, body);
            } else if (dest.endsWith(SUFFIX_CURSOR)) {
                handleCursorUpdateMessage(dest, body);
            } else if (dest.endsWith(SUFFIX_ONLINE_USERS)) {
                handleOnlineUsersMessage(dest, body);
            } else {
                log.warn("⚠️ 알 수 없는 채널: {}", channel);
            }
        } catch (Exception e) {
            log.error("❌ 메시지 처리 실패 - Channel: {}, Body: {}", channel, body, e);
        }
    }

    // ==================== Publish 메서드들 (STOMP → Redis Pub/Sub) ====================
    
    /**
     * YJS 업데이트를 Redis Pub/Sub으로 발행
     * 다른 서버들에게 브로드캐스트하여 해당 서버의 클라이언트들에게 전달
     * 
     * TTL 갱신:
     * - 문서 편집 = 사용자가 활동 중
     * - 온라인 사용자 TTL 갱신하여 장시간 작업 시에도 유지
     */
    public void publishYjsUpdateToRedis(Long documentId, String yjsUpdateJson) {
        log.debug("📤 YJS 업데이트 발행 - DocumentId: {}", documentId);
        String channel = TOPIC_PREFIX + documentId + SUFFIX_YJS_UPDATE;
        documentPubSubTemplate.convertAndSend(channel, yjsUpdateJson);
        
        // 온라인 사용자 TTL 갱신 (활동 중이므로 연장)
        String onlineKey = ONLINE_USERS_KEY + documentId;
        documentOnlineUsersTemplate.expire(onlineKey, ONLINE_USERS_TTL_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * 문서 내용 업데이트 (YJS 편집 시)
     * Redis 저장 + dirty flag 자동 설정
     */
    public void updateDocumentContent(Long documentId, String textContent) {
        documentSyncService.updateDocumentContent(documentId, textContent);
    }

    /**
     * 사용자 접속 처리
     */
    public void publishUserJoinToRedis(Long documentId, UserJoinLeaveDto joinDto) {
        log.info("👤 사용자 접속 - DocumentId: {}, UserId: {}, UserName: {}",
            documentId, joinDto.getUserId(), joinDto.getUserName());

        try {
            // 1. Redis에 온라인 사용자 추가
            String onlineKey = ONLINE_USERS_KEY + documentId;
            documentOnlineUsersTemplate.opsForHash().put(
                onlineKey, 
                joinDto.getUserId().toString(), 
                joinDto.getUserName()
            );
            // TTL 설정 (비정상 종료 대비)
            documentOnlineUsersTemplate.expire(onlineKey, ONLINE_USERS_TTL_MINUTES, TimeUnit.MINUTES);
            
            // 2. 현재 온라인 사용자 목록 조회
            Map<String, String> onlineUsers = getOnlineUsers(documentId);
            
            // 3. Redis Pub/Sub으로 다른 서버들에게 브로드캐스트
            String channel = TOPIC_PREFIX + documentId + SUFFIX_ONLINE_USERS;
            String message = objectMapper.writeValueAsString(onlineUsers);
            documentPubSubTemplate.convertAndSend(channel, message);

        } catch (Exception e) {
            log.error("❌ 사용자 접속 처리 실패 - DocumentId: {}, UserId: {}",
                documentId, joinDto.getUserId(), e);
        }
    }

    /**
     * 사용자 이탈 처리
     * 
     * 중요: 마지막 사용자가 나가면 즉시 DB에 저장
     */
    public void publishUserLeaveToRedis(Long documentId, UserJoinLeaveDto leaveDto) {
        log.info("👋 사용자 이탈 - DocumentId: {}, UserId: {}, UserName: {}", 
            documentId, leaveDto.getUserId(), leaveDto.getUserName());
        
        try {
            // 1. Redis에서 온라인 사용자 제거
            String onlineKey = ONLINE_USERS_KEY + documentId;
            documentOnlineUsersTemplate.opsForHash().delete(onlineKey, leaveDto.getUserId().toString());
            
            // 2. 커서 정보도 제거
            String cursorsKey = CURSORS_KEY + documentId;
            documentCursorsTemplate.opsForHash().delete(cursorsKey, leaveDto.getUserId().toString());
            
            // 3. 현재 온라인 사용자 목록 조회
            Map<String, String> onlineUsers = getOnlineUsers(documentId);
            
            // 4. ⭐ 마지막 사용자가 나갔으면 즉시 DB에 저장
            if (onlineUsers.isEmpty()) {
                log.info("💾 마지막 사용자 이탈 - 문서 즉시 저장: DocumentId={}", documentId);
                try {
                    documentSyncService.syncSingleDocument(documentId);
                } catch (Exception syncException) {
                    log.error("❌ 마지막 사용자 이탈 시 문서 저장 실패 - DocumentId: {}", documentId, syncException);
                }
            }
            
            // 5. Redis Pub/Sub으로 다른 서버들에게 브로드캐스트
            String channel = TOPIC_PREFIX + documentId + SUFFIX_ONLINE_USERS;
            String message = objectMapper.writeValueAsString(onlineUsers);
            documentPubSubTemplate.convertAndSend(channel, message);
            
        } catch (Exception e) {
            log.error("❌ 사용자 이탈 처리 실패 - DocumentId: {}, UserId: {}", 
                documentId, leaveDto.getUserId(), e);
        }
    }

    /**
     * 커서 위치 업데이트
     * 
     * 성능 최적화:
     * - 프론트엔드에서 Throttle(300ms) 처리 권장
     * - Redis 저장은 빠르게, Pub/Sub도 효율적
     * - 백엔드에서는 별도 최적화 불필요 (프론트 제어가 핵심)
     * 
     * TTL 갱신:
     * - 커서 움직임 = 사용자가 활동 중
     * - 온라인 사용자 TTL도 함께 갱신하여 장시간 작업 시에도 유지
     */
    public void publishCursorUpdateToRedis(Long documentId, CursorUpdateDto cursorDto) {
        log.debug("🖱️ 커서 업데이트 - DocumentId: {}, UserId: {}", documentId, cursorDto.getUserId());
        
        try {
            // 1. Redis에 커서 위치 저장 (최신 위치만 유지)
            String cursorsKey = CURSORS_KEY + documentId;
            String cursorData = objectMapper.writeValueAsString(Map.of(
                "position", cursorDto.getPosition(),
                "selection", cursorDto.getSelection() != null ? cursorDto.getSelection() : Map.of(),
                "timestamp", System.currentTimeMillis()
            ));
            documentCursorsTemplate.opsForHash().put(cursorsKey, cursorDto.getUserId().toString(), cursorData);
            documentCursorsTemplate.expire(cursorsKey, CURSORS_TTL_MINUTES, TimeUnit.MINUTES);
            
            // 1-1. 온라인 사용자 TTL 갱신 (활동 중이므로 연장)
            String onlineKey = ONLINE_USERS_KEY + documentId;
            documentOnlineUsersTemplate.expire(onlineKey, ONLINE_USERS_TTL_MINUTES, TimeUnit.MINUTES);
            
            // 2. 브로드캐스트용 DTO 생성
            CursorBroadcastDto broadcastDto = CursorBroadcastDto.builder()
                .userId(cursorDto.getUserId())
                .userName(cursorDto.getUserName())
                .position(cursorDto.getPosition())
                .selection(cursorDto.getSelection())
                .build();
            
            // 3. Redis Pub/Sub으로 다른 서버들에게 브로드캐스트
            // Redis Pub/Sub은 매우 빠르므로 병목 없음
            String channel = TOPIC_PREFIX + documentId + SUFFIX_CURSOR;
            String message = objectMapper.writeValueAsString(broadcastDto);
            documentPubSubTemplate.convertAndSend(channel, message);
            
        } catch (Exception e) {
            log.error("❌ 커서 업데이트 실패 - DocumentId: {}, UserId: {}", 
                documentId, cursorDto.getUserId(), e);
        }
    }

    /**
     * 문서 동기화 요청 처리
     * 초기 접속 시 현재 온라인 사용자 및 커서 정보 반환
     */
    public void handleDocumentSync(Long documentId) {
        log.info("🔄 문서 동기화 요청 - DocumentId: {}", documentId);
        
        try {
            // 1. 온라인 사용자 목록 조회
            Map<String, String> onlineUsers = getOnlineUsers(documentId);
            
            // 2. 커서 위치들 조회
            Map<String, Object> cursors = getAllCursors(documentId);
            
            // 3. 동기화 응답 데이터 구성
            Map<String, Object> syncResponse = Map.of(
                "onlineUsers", onlineUsers,
                "cursors", cursors,
                "timestamp", System.currentTimeMillis()
            );
            
            // 4. 요청한 클라이언트에게 응답
            messagingTemplate.convertAndSend(TOPIC_PREFIX + documentId + "/sync", syncResponse);
            
        } catch (Exception e) {
            log.error("❌ 문서 동기화 실패 - DocumentId: {}", documentId, e);
        }
    }

    // ==================== Redis Pub/Sub 메시지 핸들러들 ====================
    
    private void handleYjsUpdateMessage(String dest, String body) {
        try {
            // Redis에서 받은 YJS 업데이트를 STOMP 클라이언트들에게 전달
            messagingTemplate.convertAndSend(dest, body);
            log.debug("✅ YJS 업데이트 브로드캐스트 완료 - Dest: {}", dest);
        } catch (Exception e) {
            log.error("❌ YJS 업데이트 메시지 처리 실패", e);
        }
    }

    private void handleCursorUpdateMessage(String dest, String body) {
        try {
            CursorBroadcastDto cursorDto = objectMapper.readValue(body, CursorBroadcastDto.class);
            messagingTemplate.convertAndSend(dest, cursorDto);
            log.debug("✅ 커서 업데이트 브로드캐스트 완료 - Dest: {}", dest);
        } catch (JsonProcessingException e) {
            log.error("❌ 커서 업데이트 메시지 파싱 실패", e);
        }
    }

    private void handleOnlineUsersMessage(String dest, String body) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> onlineUsers = objectMapper.readValue(body, Map.class);
            messagingTemplate.convertAndSend(dest, onlineUsers);
            log.debug("✅ 온라인 사용자 목록 브로드캐스트 완료 - Dest: {}, 인원: {}", dest, onlineUsers.size());
        } catch (JsonProcessingException e) {
            log.error("❌ 온라인 사용자 메시지 파싱 실패", e);
        }
    }

    // ==================== Redis 데이터 조회 메서드들 ====================
    
    /**
     * 온라인 사용자 목록 조회
     */
    public Map<String, String> getOnlineUsers(Long documentId) {
        String onlineKey = ONLINE_USERS_KEY + documentId;
        Map<Object, Object> rawEntries = documentOnlineUsersTemplate.opsForHash().entries(onlineKey);
        
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<Object, Object> entry : rawEntries.entrySet()) {
            result.put(entry.getKey().toString(), entry.getValue().toString());
        }
        
        return result;
    }


    // 모든 커서 위치 조회
    public Map<String, Object> getAllCursors(Long documentId) {
        String cursorsKey = CURSORS_KEY + documentId;
        Map<Object, Object> rawEntries = documentCursorsTemplate.opsForHash().entries(cursorsKey);
        
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<Object, Object> entry : rawEntries.entrySet()) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> cursorData = objectMapper.readValue(
                    entry.getValue().toString(), 
                    Map.class
                );
                result.put(entry.getKey().toString(), cursorData);
            } catch (JsonProcessingException e) {
                log.error("❌ 커서 데이터 파싱 실패 - UserId: {}", entry.getKey(), e);
            }
        }
        
        return result;
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
