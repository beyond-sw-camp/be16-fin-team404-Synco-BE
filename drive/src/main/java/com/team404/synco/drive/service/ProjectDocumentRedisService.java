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
    // 워크스페이스 멤버 확인용 (Redis DB 2)
    private static final String WORKSPACE_KEY_PREFIX = "workSpaceSeq:";
    private static final String FRIEND_LIST = "friendList";

    // TTL 설정 (안전장치: 비정상 종료 시 자동 정리)
    private static final int ONLINE_USERS_TTL_MINUTES = 30;
    // STOMP Topics
    private static final String TOPIC_PREFIX = "/topic/document/";
    private static final String SUFFIX_DOCUMENT_UPDATE = "/document-update";
    private static final String SUFFIX_ONLINE_USERS = "/online-users";

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
    public void publishDocumentUpdateToRedis(Long documentId, UpdateDocumentReqDto updateDocumentReqDto) {
        try {
            // Redis Pub/Sub으로 다른 서버들에게 브로드캐스트
            String channel = TOPIC_PREFIX + documentId + SUFFIX_DOCUMENT_UPDATE;
            String message = objectMapper.writeValueAsString(updateDocumentReqDto);
            documentPubSubTemplate.convertAndSend(channel, message);

        } catch (Exception e) {
            log.error("❌ 문서 업데이트 발행 실패 - DocumentId: {}, ResponseDto: {}",
                    documentId, updateDocumentReqDto, e);
        }
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
     */
    public void publishUserLeaveToRedis(Long documentId, UserJoinLeaveDto leaveDto) {
        log.info("👋 사용자 이탈 - DocumentId: {}, UserId: {}, UserName: {}",
            documentId, leaveDto.getUserId(), leaveDto.getUserName());

        try {
            // 1. Redis에서 온라인 사용자 제거
            String onlineKey = ONLINE_USERS_KEY + documentId;
            documentOnlineUsersTemplate.opsForHash().delete(onlineKey, leaveDto.getUserId().toString());

            // 2. 현재 온라인 사용자 목록 조회
            Map<String, String> onlineUsers = getOnlineUsers(documentId);
            // 3. Redis Pub/Sub으로 다른 서버들에게 브로드캐스트
            String channel = TOPIC_PREFIX + documentId + SUFFIX_ONLINE_USERS;
            String message = objectMapper.writeValueAsString(onlineUsers);
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
        String dest = normalizeChannel(channel);

        if (channel.contains(SUFFIX_DOCUMENT_UPDATE)) {
            handleLineUpdateMessage(dest, body);
        } else if (channel.contains(SUFFIX_ONLINE_USERS)) {
            handleOnlineUsersMessage(dest, body);
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
