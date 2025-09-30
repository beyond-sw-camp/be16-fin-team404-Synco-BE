package com.team404.synco.workspace.controller;

import com.team404.synco.common.dto.ResponseDto;
import com.team404.synco.workspace.dto.WorkSpaceCreateReqDto;
import com.team404.synco.workspace.service.WorkSpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/workspace")
public class WorkSpaceController {
    private final WorkSpaceService workSpaceService;

    // 워크스페이스 생성
    @PostMapping("/create")
    public ResponseEntity<?> createWorkSpace(@RequestBody WorkSpaceCreateReqDto workSpaceCreateReqDto, @RequestHeader("X-User-Id")Long userId){
        Long id = workSpaceService.createTeamWorkSpace(workSpaceCreateReqDto);
        return new ResponseEntity<>(ResponseDto.ok(id, HttpStatus.OK), HttpStatus.OK);
    }
    // 워크스페이스 수정
    // 워크스페이스 삭제
    // 내 워크스페이스 목록 조회
    // 워크스페이스 초대
    // 채널 생성
    // 채널 수정
    // 채널 삭제
}
