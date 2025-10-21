package com.team404.synco.workspace.service;

import com.team404.synco.workspace.dto.DriveCreateReqDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "drive-service")
public interface DriveFeign {
    @PostMapping("/drive/personal/create")
    void createPersonalDrive(@RequestBody DriveCreateReqDto driveCreateReqDto);

    @PostMapping("/drive/project/create")
    void createTeamDrive(@RequestBody DriveCreateReqDto driveCreateReqDto);

    @DeleteMapping("/drive/project/deleteDrive/{workSpaceSeq}")
    void deleteTeamDrive(@PathVariable("workSpaceSeq") Long workSpaceSeq);
}
