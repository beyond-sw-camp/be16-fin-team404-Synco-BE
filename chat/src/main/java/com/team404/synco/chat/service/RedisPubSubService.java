package com.team404.synco.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team404.synco.chat.dto.ChatMessageResDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class RedisPubSubService implements MessageListener {
    private final StringRedisTemplate stringRedisTemplate;
    private final SimpMessageSendingOperations messageTemplate;

    public RedisPubSubService(@Qualifier("chatPubSub") StringRedisTemplate stringRedisTemplate, SimpMessageSendingOperations messageTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.messageTemplate = messageTemplate;
    }

    public void publish(String channel, String message) {
        stringRedisTemplate.convertAndSend(channel, message);
    }

//    Redis에서 발행된 채팅 메시지를 받아 → 객체로 복원하고 → WebSocket(STOMP)을 통해 구독자에게 보냄
    @Override
//    pattern에는 topic의 이름의 패턴이 담겨있고, 이 패턴을 기반으로 다이나믹한 코딩
    public void onMessage(Message message, byte[] pattern) {
        System.out.println("meessage : " + message);
        String payload = new String(message.getBody()); // byte[] -> String으로 변환
        System.out.println("payload : " + payload);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // ✅ action 필드 확인 (TYPING 또는 DELETE)
            if (payload.contains("\"action\"")) {
                Map<String, Object> event = objectMapper.readValue(payload, Map.class);
                String action = event.get("action").toString();
                String channelSeq = event.get("channelSeq").toString();

                if ("DELETE".equals(action)) {
                    log.info("🗑️ DELETE broadcast: {}", payload);
                }

                if ("TYPING".equals(action)) {
                    log.info("⌨️ TYPING broadcast: {}", payload);
                }

                // ✅ 공통 처리: 해당 채널 subscriber 에게 broadcast
                messageTemplate.convertAndSend("/topic/" + channelSeq, event);
                return;
            }

            // 일반 메시지 브로드캐스트
            ChatMessageResDto chatMessageResDto = objectMapper.readValue(payload, ChatMessageResDto.class);  // String -> dto 역직렬화
//            STOMP(WebSocket)으로 해당 방의 구독자들에게 메시지를 전송.
            messageTemplate.convertAndSend("/topic/"+chatMessageResDto.getChannelSeq(), chatMessageResDto);
        } catch (Exception e) {
            log.error("❌ Redis message handling failed", e);
        }
    }
}
