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
                                                  @RequestHeader("X-member-seq") Long memberSeq);

    @PostMapping("/virtual-meeting/addMember")
    void addMemberToTaskChannel(@RequestBody ChannelInviteReqDto channelInviteReqDto);

    @PostMapping("/virtual-meeting/createChannel")
    void createVirtualMeetChannel(@RequestBody ChannelCreateReqDto virtualMeetingChannelCreateReqDto);

    @PostMapping("/virtual-meeting/addMember")
    void addMemberToVirtualMeetingChannel(@RequestBody ChannelInviteReqDto channelInviteReqDto);

    @DeleteMapping("/virtual-meeting/{workSpaceSeq}")
    void deleteAllVirtualMeetingChannel(@PathVariable Long workSpaceSeq);

    @PatchMapping("/virtual-meeting/delegateSuperAuthority")
    void delegateVirtualMeetChannelSuperAuthority(@RequestBody DelegateSuperAuthorityReqDto delegateSuperAuthorityReqDto,
                                @RequestHeader("X-member-seq") Long memberSeq);
}
