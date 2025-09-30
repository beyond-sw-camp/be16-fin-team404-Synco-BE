package com.team404.synco.workspace.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "task-service")
public interface TaskFeign {
    @PostMapping("/task/create-channel")
    Long createTaskChannel();

    @PostMapping("/task/virtual-meet/create-channel")
    Long createVirtualMeetChannel();
}
