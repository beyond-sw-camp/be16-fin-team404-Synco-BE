package com.team404.synco.alarm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseNotificationPublisher {
    
    @Qualifier("ssePubSub")
    private final RedisTemplate<String, String> redisTemplate;
    
    // Redis 채널로 상태 변경 알림 메시지 발행
    public void publish(Long targetMemberSeq, String messageJson) {
        try {
            // 대상 사용자 ID와 메시지를 함께 전송
            String payload = targetMemberSeq + ":" + messageJson;
            redisTemplate.convertAndSend("status-update", payload);
        } catch (Exception e) {
            throw new RuntimeException("Redis Pub/Sub 상태 변경 알림 발행 실패", e);
        }
    }
    
}
