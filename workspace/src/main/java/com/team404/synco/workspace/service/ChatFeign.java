package com.team404.synco.workspace.service;

import com.team404.synco.workspace.dto.ChatChannelCreateReqDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "chat-service")
public interface ChatFeign {
    @PostMapping("/chat/create-channel")
    void createChatChannel(@RequestBody ChatChannelCreateReqDto chatChannelCreateReqDto);
}
