package com.team404.synco.workspace.service;

import com.team404.synco.workspace.dto.ChannelCreateReqDto;
import com.team404.synco.workspace.dto.ChannelInviteReqDto;
import com.team404.synco.workspace.dto.TaskChannelMemberCreateReqDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "task-service")
public interface TaskFeign {
    @PostMapping("/task/create")
    void createTask(@RequestBody TaskChannelMemberCreateReqDto taskChannelMemberCreateReqDto);

    @PostMapping("/virtual-meeting/create-channel")
    void createVirtualMeetChannel(@RequestBody ChannelCreateReqDto virtualMeetingChannelCreateReqDto);

    @PostMapping("/virtual-meeting/addMember")
    void addMemberToVirtualMeetingChannel(@RequestBody ChannelInviteReqDto channelInviteReqDto);
}
