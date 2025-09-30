package com.team404.synco.workspace.service;

import com.team404.synco.workspace.dto.ChatChannelCreateReqDto;
import com.team404.synco.workspace.dto.VirtualMeetingChannelCreateReqDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "task-service")
public interface TaskFeign {
    @PostMapping("/task/create-channel")
    Long createTask();

    @PostMapping("/task/virtual-meet/create-channel")
    void createVirtualMeetChannel(@RequestBody VirtualMeetingChannelCreateReqDto virtualMeetingChannelCreateReqDto);
}
