package com.team404.synco.workspace.controller;

import com.team404.synco.common.dto.ResponseDto;
import com.team404.synco.workspace.dto.WorkSpaceCreateReqDto;
import com.team404.synco.workspace.service.WorkSpaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/workspace")
public class WorkSpaceController {
    private final WorkSpaceService workSpaceService;

    // 팀 워크스페이스 생성
    @PostMapping("/create")
    public ResponseEntity<?> createWorkSpace(@ModelAttribute WorkSpaceCreateReqDto workSpaceCreateReqDto, @RequestHeader("X-User-Id")Long userId){
        log.info("요청값 : {}", workSpaceCreateReqDto.getWorkSpaceName());
        log.info("파일명 : {}", workSpaceCreateReqDto.getWorkSpaceThumbnailImage());
        Long id = workSpaceService.createTeamWorkSpace(workSpaceCreateReqDto, userId);
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
