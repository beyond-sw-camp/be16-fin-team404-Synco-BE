package com.team404.synco.drive.listener;

import com.team404.synco.drive.dto.UserJoinLeaveDto;
import com.team404.synco.drive.service.ProjectDocumentRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// Spring과 Stomp는 기본적으로 세션관리를 자동(내부적)으로 처리
// 연결/해제 이벤트를 기록, 변경된 세션수를 실시간으로 확인할 목적으로 이벤트 리스너를 생성 => 로그, 디버깅 목적
@Component
@RequiredArgsConstructor
@Slf4j
public class StompEventListener {

    private final ProjectDocumentRedisService projectDocumentRedisService;
    private final Set<String> sessions = ConcurrentHashMap.newKeySet();

//    연결했을때 이벤트 핸들러
    @EventListener
    public void connectHandle(SessionConnectEvent event){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        sessions.add(accessor.getSessionId());
        logSession(true, accessor);
    }

//    연결 해제했을때 이벤트 핸들러
    @EventListener
    public void disconnectHandle(SessionDisconnectEvent event){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        
        // 세션에서 저장된 정보 가져오기 (SUBSCRIBE 시 저장됨)
        Long documentId = (Long) accessor.getSessionAttributes().get("documentId");
        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        
        // 자동 이탈 처리 (비정상 종료 대비)
        if (documentId != null && userId != null) {
            try {
                // Redis에서 userName 조회
                Map<String, String> onlineUsers = projectDocumentRedisService.getOnlineUsers(documentId);
                String userName = onlineUsers.get(userId.toString());
                
                // 이탈 처리
                UserJoinLeaveDto leaveDto = UserJoinLeaveDto.builder()
                    .userId(userId)
                    .userName(userName != null ? userName : "Unknown")
                    .build();
                
                projectDocumentRedisService.publishUserLeaveToRedis(documentId, leaveDto);
                log.info("🔌 WebSocket DISCONNECT - 자동 이탈 처리 완료: DocumentId={}, UserId={}, UserName={}", 
                    documentId, userId, userName);
            } catch (Exception e) {
                log.error("❌ 자동 이탈 처리 실패 - DocumentId: {}, UserId: {}", documentId, userId, e);
            }
        }
        
        sessions.remove(accessor.getSessionId());
        logSession(false, accessor);
    }

    private void logSession(Boolean isConnect, StompHeaderAccessor accessor) {
        log.info("{}connect sessionId={}", isConnect ? "" : "dis", accessor.getSessionId());
        log.info("total sessions={}", sessions.size());
    }
}
