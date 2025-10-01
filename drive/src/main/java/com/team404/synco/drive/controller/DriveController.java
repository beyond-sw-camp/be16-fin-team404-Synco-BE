package com.team404.synco.drive.controller;

import com.team404.synco.common.dto.ResponseDto;
import com.team404.synco.drive.dto.DriveChannelCreateReqDto;
import com.team404.synco.drive.service.DriveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/drive")
public class DriveController {
    private final DriveService driveService;
    // 드라이브 채널 생성
    @PostMapping("/create-channel")
    public ResponseEntity<?> createChannel(@RequestBody DriveChannelCreateReqDto driveChannelCreateReqDto){
        Long id = driveService.createChannel(driveChannelCreateReqDto);
        return new ResponseEntity<>(ResponseDto.ok(id, HttpStatus.OK), HttpStatus.OK);
    }
}
