package com.team404.synco.chat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team404.synco.chat.dto.ChatMessageReqDto;
import com.team404.synco.chat.service.ChatService;
import com.team404.synco.chat.service.RedisPubSubService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class StompController {
    private final ChatService chatService;
    private final RedisPubSubService redisPubSubService;

    public StompController(ChatService chatService, RedisPubSubService redisPubSubService) {
        this.chatService = chatService;
        this.redisPubSubService = redisPubSubService;
    }

//    클라이언트가 보낸 메시지를 서버가 받아서 처리하고 Redis로 publish
    @MessageMapping("/{channelReq}")
    public void sendMessage(@DestinationVariable Long channelReq, ChatMessageReqDto chatMessageReqDto) throws JsonProcessingException {
        log.info("메시지 본문 : {}", chatMessageReqDto.getChatMessageText());

        chatService.saveMessage(channelReq, chatMessageReqDto); //메시지 저장

        chatMessageReqDto.setChannelSeq(channelReq);
        System.out.println("chatMessageReqDto : " + chatMessageReqDto);
        ObjectMapper objectMapper = new ObjectMapper();
        String message = objectMapper.writeValueAsString(chatMessageReqDto);
        redisPubSubService.publish("chat", message);
    }
}
