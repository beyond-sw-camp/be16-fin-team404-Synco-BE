package com.team404.synco.drive.controller;

import com.team404.synco.common.dto.ResponseDto;
import com.team404.synco.drive.dto.DriveCreateReqDto;
import com.team404.synco.drive.service.TeamDriveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/drive/team")
public class TeamDriveController {
    private final TeamDriveService teamDriveService;
    // 드라이브 생성
    @PostMapping("/create")
    public ResponseEntity<ResponseDto<?>>createChannel(@RequestBody DriveCreateReqDto driveCreateReqDto){
        Long id = teamDriveService.createChannel(driveCreateReqDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.ok(id, HttpStatus.CREATED));
    }
}
