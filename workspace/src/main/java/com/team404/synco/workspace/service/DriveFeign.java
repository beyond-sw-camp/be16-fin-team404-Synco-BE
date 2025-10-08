package com.team404.synco.workspace.service;

import com.team404.synco.workspace.dto.DriveCreateReqDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "drive-service")
public interface DriveFeign {
    @PostMapping("/drive/personal/create")
    void createPersonalDrive(@RequestBody DriveCreateReqDto driveCreateReqDto);
    @PostMapping("/drive/team/create")
    void createTeamDrive(@RequestBody DriveCreateReqDto driveCreateReqDto);
}
