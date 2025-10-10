package com.team404.synco.workspace.service;

import com.team404.synco.workspace.dto.ChannelCreateReqDto;
import com.team404.synco.workspace.dto.ChannelInviteReqDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "chat-service")
public interface ChatFeign {
    @PostMapping("/chat/createChannel")
    void createChatChannel(@RequestBody ChannelCreateReqDto channelCreateReqDto);

    @PostMapping("/chat/addMember")
    void addMemberToChannel(@RequestBody ChannelInviteReqDto channelInviteReqDto);

    @DeleteMapping("/chat/{workSpaceSeq}")
    void deleteAllChannel(@PathVariable Long workSpaceSeq);
}
