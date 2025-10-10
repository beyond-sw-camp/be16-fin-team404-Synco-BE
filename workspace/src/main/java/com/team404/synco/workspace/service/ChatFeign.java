package com.team404.synco.workspace.service;

import com.team404.synco.workspace.dto.ChannelCreateReqDto;
import com.team404.synco.workspace.dto.ChannelInviteReqDto;
import com.team404.synco.workspace.dto.DelegateSuperAuthorityReqDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "chat-service")
public interface ChatFeign {
    @PostMapping("/chat/createChannel")
    void createChatChannel(@RequestBody ChannelCreateReqDto channelCreateReqDto);

    @PostMapping("/chat/addMember")
    void addMemberToChannel(@RequestBody ChannelInviteReqDto channelInviteReqDto);

    @DeleteMapping("/chat/{workSpaceSeq}")
    void deleteAllChannel(@PathVariable Long workSpaceSeq);

    @PatchMapping("/chat/delegateSuperAuthority")
    void delegateSuperAuthority(@RequestBody DelegateSuperAuthorityReqDto delegateSuperAuthorityReqDto,
                                @RequestHeader("X-member-seq") Long memberSeq);
}
