package com.team404.synco.chat.config;

import com.team404.synco.chat.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StompHandler implements ChannelInterceptor {

    private final ChatService chatService;

    public StompHandler(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message); // STOMP 메시지 헤더 추출

        if (StompCommand.CONNECT == accessor.getCommand()) {
            log.info("CONNECT 요청 수신 - Gateway 인증 헤더 확인");

            String memberSeqHeader = accessor.getFirstNativeHeader("X-Member-Seq");
            if (memberSeqHeader == null) {
                throw new AuthenticationServiceException("인증 정보(X-Member-Seq)가 없습니다.");
            }

            log.info("인증 완료 - memberSeq: {}", memberSeqHeader);
        }

        if (StompCommand.SUBSCRIBE == accessor.getCommand()) {
            String memberSeqHeader = accessor.getFirstNativeHeader("X-Member-Seq");
            if (memberSeqHeader == null) {
                throw new AuthenticationServiceException("인증 정보(X-Member-Seq)가 없습니다.");
            }

            Long memberSeq = Long.parseLong(memberSeqHeader);

            // "/sub/channel/{channelId}" 형태라고 가정
            String destination = accessor.getDestination();
            if (destination == null || !destination.contains("/channel/")) {
                throw new AuthenticationServiceException("잘못된 채널 구독 요청입니다. destination=" + destination);
            }

            String channelId = destination.split("/channel/")[1];
            log.info("SUBSCRIBE 요청 - memberSeq={}, channelId={}", memberSeq, channelId);

            // ✅채널 참여 여부 검증
            if (!chatService.isChannelParticipant(memberSeq, Long.parseLong(channelId))) {  //채널 참여여부 확인
                log.error("채널 접근 권한 없음 - memberSeq={}, channelId={}", memberSeq, channelId);
                throw new AuthenticationServiceException("해당 채널에 접근 권한이 없습니다.");
            }
        }
        return message;
    }
}