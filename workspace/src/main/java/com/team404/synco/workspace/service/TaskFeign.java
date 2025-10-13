package com.team404.synco.workspace.service;

import com.team404.synco.workspace.dto.ChannelCreateReqDto;
import com.team404.synco.workspace.dto.ChannelInviteReqDto;
import com.team404.synco.workspace.dto.DelegateSuperAuthorityReqDto;
import com.team404.synco.workspace.dto.TaskChannelMemberCreateReqDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "task-service")
public interface TaskFeign {
    @PostMapping("/task/create")
    void createTask(@RequestBody TaskChannelMemberCreateReqDto taskChannelMemberCreateReqDto);

    @DeleteMapping("/task/{workSpaceSeq}")
    void deleteTaskChannel(@PathVariable Long workSpaceSeq);

    @PatchMapping("/task/delegateSuperAuthority")
    void delegateTaskChannelSuperAuthority(@RequestBody DelegateSuperAuthorityReqDto delegateSuperAuthorityReqDto,
                                           @RequestHeader("X-Member-Seq") Long memberSeq);

    @PostMapping("/task/addMember")
    void addMemberToTaskChannel(@RequestBody ChannelInviteReqDto channelInviteReqDto);

    @PostMapping("/virtual-meeting/createBasicChannel")
    void createVirtualMeetBasicChannel(@RequestBody ChannelCreateReqDto channelCreateReqDto);

    @PostMapping("/virtual-meeting/addMember")
    void addMemberToVirtualMeetingChannel(@RequestBody ChannelInviteReqDto channelInviteReqDto,
                                          @RequestHeader("X-Member-Seq") Long memberSeq);

    @DeleteMapping("/virtual-meeting/{workSpaceSeq}")
    void deleteAllVirtualMeetingChannel(@PathVariable Long workSpaceSeq);

    @PatchMapping("/virtual-meeting/delegateSuperAuthority")
    void delegateVirtualMeetChannelSuperAuthority(@RequestBody DelegateSuperAuthorityReqDto delegateSuperAuthorityReqDto,
                                                  @RequestHeader("X-Member-Seq") Long memberSeq);
}
