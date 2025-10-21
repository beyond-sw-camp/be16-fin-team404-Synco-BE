package com.team404.synco.workspace.service;

import com.team404.synco.workspace.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "task-service")
public interface TaskFeign {
    @PostMapping("/task/create")
    void createTask(@RequestBody TaskChannelMemberCreateReqDto taskChannelMemberCreateReqDto);

    @DeleteMapping("/task/{workSpaceSeq}")
    void deleteTaskChannel(@PathVariable("workSpaceSeq") Long workSpaceSeq);

    @PostMapping("/task/delegateSuperAuthority")
    void delegateTaskChannelSuperAuthority(@RequestBody DelegateSuperAuthorityReqDto delegateSuperAuthorityReqDto,
                                           @RequestHeader("X-Member-Seq") Long memberSeq);

    @PostMapping("/task/addMember")
    void addMemberToTaskChannel(@RequestBody ChannelInviteReqDto channelInviteReqDto);

    @GetMapping("/task/memberList")
    List<Long> findMyWorkSpaceList(@RequestHeader("X-Member-Seq") Long memberSeq);

    @GetMapping("/task/{workSpaceSeq}/members")
    List<Long> findWorkSpaceMemberList(@PathVariable("workSpaceSeq") Long workSpaceSeq);

    @DeleteMapping("/task/leave/{workSpaceSeq}")
    void leaveWorkSpaceFromTask(@PathVariable("workSpaceSeq")Long workSpaceSeq, @RequestHeader("X-Member-Seq") Long memberSeq);

    @DeleteMapping("/task/kick")
    void kickFromWorkSpaceTask(@RequestBody KickMemberFromWorkSpaceReqDto kickMemberFromWorkSpaceReqDto);

    @PostMapping("/virtual-meeting/createBasicChannel")
    void createVirtualMeetBasicChannel(@RequestBody ChannelCreateReqDto channelCreateReqDto);

    @PostMapping("/virtual-meeting/addMember")
    void addMemberToVirtualMeetingChannel(@RequestBody ChannelInviteReqDto channelInviteReqDto,
                                          @RequestHeader("X-Member-Seq") Long memberSeq);

    @DeleteMapping("/virtual-meeting/{workSpaceSeq}")
    void deleteAllVirtualMeetingChannel(@PathVariable("workSpaceSeq") Long workSpaceSeq);

    @PostMapping("/virtual-meeting/delegateSuperAuthority")
    void delegateVirtualMeetChannelSuperAuthority(@RequestBody DelegateSuperAuthorityReqDto delegateSuperAuthorityReqDto,
                                                  @RequestHeader("X-Member-Seq") Long memberSeq);

    @DeleteMapping("/virtual-meeting/leave/{workSpaceSeq}")
    void leaveWorkSpaceFromVirtualMeeting(@PathVariable("workSpaceSeq")Long workSpaceSeq, @RequestHeader("X-Member-Seq") Long memberSeq);

    @DeleteMapping("/virtual-meeting/kick")
    void kickFromWorkSpaceVirtualMeeting(@RequestBody KickMemberFromWorkSpaceReqDto kickMemberFromWorkSpaceReqDto);
}
