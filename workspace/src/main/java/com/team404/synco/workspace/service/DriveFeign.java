package com.team404.synco.workspace.service;

import com.team404.synco.workspace.dto.DriveChannelCreateReqDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "drive-service")
public interface DriveFeign {
    @PostMapping("/drive/create-channel")
    void createDriveChannel(DriveChannelCreateReqDto 기본);
}
