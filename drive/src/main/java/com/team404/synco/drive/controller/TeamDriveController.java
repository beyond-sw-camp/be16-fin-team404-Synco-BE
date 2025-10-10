package com.team404.synco.drive.controller;

import com.team404.synco.common.dto.ResponseDto;
import com.team404.synco.drive.dto.DriveCreateReqDto;
import com.team404.synco.drive.service.TeamDriveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // 팀 드라이브 삭제(워크스페이스 삭제시)
    @DeleteMapping("/{workSpaceSeq}")
    public void deleteAllChannel(@PathVariable Long workSpaceSeq){
        teamDriveService.deleteDrive(workSpaceSeq);
    }
}
