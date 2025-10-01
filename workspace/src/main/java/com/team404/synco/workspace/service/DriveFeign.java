package com.team404.synco.workspace.service;

import com.team404.synco.workspace.dto.DriveChannelCreateReqDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "drive-service")
public interface DriveFeign {
    @PostMapping("/drive/create-channel")
    void createDriveChannel(@RequestBody DriveChannelCreateReqDto driveChannelCreateReqDto);
}
