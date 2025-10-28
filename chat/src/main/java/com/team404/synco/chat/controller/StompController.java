package com.team404.synco.chat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team404.synco.chat.dto.ChatMessageReqDto;
import com.team404.synco.chat.dto.ChatMessageResDto;
import com.team404.synco.chat.dto.ChatTypingDto;
import com.team404.synco.chat.service.ChatService;
import com.team404.synco.chat.service.RedisPubSubService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class StompController {
    private final ChatService chatService;
    private final RedisPubSubService redisPubSubService;
    private final ObjectMapper objectMapper;

    public StompController(ChatService chatService, RedisPubSubService redisPubSubService, ObjectMapper objectMapper) {
        this.chatService = chatService;
        this.redisPubSubService = redisPubSubService;
        this.objectMapper = objectMapper;
    }

    // 클라이언트가 보낸 메시지를 서버가 받아서 처리하고 Redis로 publish
    @MessageMapping("/{channelSeq}")
    public void sendMessage(@DestinationVariable Long channelSeq, ChatMessageReqDto chatMessageReqDto)
            throws JsonProcessingException {
        log.info("메시지 본문 : {}", chatMessageReqDto.getChatMessageText());

        ChatMessageResDto chatMessageResDto = chatService.saveMessage(channelSeq, chatMessageReqDto); // 메시지 저장
        System.out.println("chatMessageResDto : " + chatMessageResDto);

        redisPubSubService.publish(
                "chat:" + channelSeq, // ✅ 채널별 토픽 분리
                objectMapper.writeValueAsString(chatMessageResDto)
        );
    }

    // 타이핑 이벤트
    @MessageMapping("/typing")
    public void typing(ChatTypingDto dto) {
        log.debug("⌨️ Typing event: {}", dto);
        chatService.publishTyping(dto);
    }
}
