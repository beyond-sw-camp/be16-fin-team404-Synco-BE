package com.team404.synco.chat.config;

import com.team404.synco.chat.security.JwtUtil;
import com.team404.synco.chat.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StompHandler implements ChannelInterceptor {

    private final ChatService chatService;
    private final JwtUtil jwtUtil;

    public StompHandler(ChatService chatService, JwtUtil jwtUtil) {
        this.chatService = chatService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        System.out.println("heeer");
        // ✅ CONNECT 시 토큰 검증 (Gateway 안 탐)
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");
            if (token == null) {
                throw new IllegalArgumentException("토큰이 없습니다.");
            }

            Long memberSeq = jwtUtil.extractMemberSeq(token);
            if (memberSeq == null) {
                throw new IllegalArgumentException("JWT 토큰이 유효하지 않습니다.");
            }

            // 세션에 memberSeq 저장
            accessor.getSessionAttributes().put("memberSeq", memberSeq);
            log.info("STOMP CONNECT 성공 - memberSeq={}", memberSeq);
        }

        // ✅ SUBSCRIBE 시 채널 접근 권한 검증
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {

            Long memberSeq = (Long) accessor.getSessionAttributes().get("memberSeq");
            if (memberSeq == null) {
                throw new IllegalArgumentException("세션에 사용자 정보가 없습니다.");
            }

            String destination = accessor.getDestination();
            if (destination == null || !destination.contains("/topic/")) {
                throw new IllegalArgumentException("잘못된 구독 요청입니다. destination=" + destination);
            }

            String channelSeq = destination.substring(destination.lastIndexOf("/") + 1);
            log.info("SUBSCRIBE 요청 - memberSeq={}, channelSeq={}", memberSeq, channelSeq);

//            if (!chatService.isChannelParticipant(memberSeq, Long.parseLong(channelSeq))) {
//                log.error("채널 접근 권한 없음 - memberSeq={}, channelSeq={}", memberSeq, channelSeq);
//                throw new IllegalArgumentException("해당 채널에 접근 권한이 없습니다.");
//            }
        }

        return message;
    }
}
