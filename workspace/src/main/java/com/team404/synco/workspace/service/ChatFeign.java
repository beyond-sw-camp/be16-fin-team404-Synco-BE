package com.team404.synco.workspace.service;

import com.team404.synco.workspace.dto.ChannelCreateReqDto;
import com.team404.synco.workspace.dto.ChannelInviteReqDto;
import com.team404.synco.workspace.dto.DelegateSuperAuthorityReqDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "chat-service")
public interface ChatFeign {
    @PostMapping("/chat/createBasicChannel")
    void createChatBasicChannel(@RequestBody ChannelCreateReqDto channelCreateReqDto);

    @PostMapping("/chat/addMember")
    void addMemberToChannel(@RequestBody ChannelInviteReqDto channelInviteReqDto,
                            @RequestHeader("X-Member-Seq") Long memberSeq);

    @DeleteMapping("/chat/{workSpaceSeq}")
    void deleteAllChannel(@PathVariable Long workSpaceSeq);

    @PostMapping("/chat/delegateSuperAuthority")
    void delegateSuperAuthority(@RequestBody DelegateSuperAuthorityReqDto delegateSuperAuthorityReqDto,
                                @RequestHeader("X-Member-Seq") Long memberSeq);
}
