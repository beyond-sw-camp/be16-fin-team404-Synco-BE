package com.team404.synco.alarm.service;

import com.team404.synco.common.service.SseEmitterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseNotificationSubscriber implements MessageListener {
    
    private final SseEmitterRegistry sseEmitterRegistry;
    
    // Redis에서 수신된 메시지 처리
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channelName = new String(pattern);
        String payload = new String(message.getBody());
        
        
        try {
            // payload 형식: "targetMemberSeq:messageJson"
            String[] parts = payload.split(":", 2);
            if (parts.length != 2) {
                return;
            }
            
            Long targetMemberSeq = Long.parseLong(parts[0]);
            String messageJson = parts[1];
            
            // 대상 사용자가 현재 서버에 연결되어 있는지 확인
            SseEmitter sseEmitter = sseEmitterRegistry.getEmitter(targetMemberSeq);
            if (sseEmitter != null) {
                sendDirectNotification(sseEmitter, messageJson);
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Redis 메시지 처리 실패", e);
        }
    }
    
    // SSE로 직접 ActiveStatus 알림 전송
    private void sendDirectNotification(SseEmitter sseEmitter, String messageJson) {
        try {
            sseEmitter.send(SseEmitter.event()
                    .name("STATUS_CHANGE")
                    .data(messageJson));
        } catch (IOException e) {
            throw new RuntimeException("SSE 직접 전송 실패", e);
        }
    }
}
