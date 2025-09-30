package com.team404.synco.chat.controller;

import com.team404.synco.chat.dto.ChatChannelCreateReqDto;
import com.team404.synco.chat.service.ChatService;
import com.team404.synco.common.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;
    @PostMapping("/create-channel")
    public ResponseEntity<?> createChannel(@RequestBody ChatChannelCreateReqDto chatChannelCreateReqDto){
        Long id = chatService.createChannel(chatChannelCreateReqDto);
        return new ResponseEntity<>(ResponseDto.ok(id, HttpStatus.OK), HttpStatus.OK);
    }
}
