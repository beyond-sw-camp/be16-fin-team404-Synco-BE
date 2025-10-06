package com.team404.synco.workspace.controller;

import com.team404.synco.common.dto.ResponseDto;
import com.team404.synco.workspace.dto.WorkSpaceCreateReqDto;
import com.team404.synco.workspace.dto.ChannelInviteReqDto;
import com.team404.synco.workspace.dto.WorkSpaceResDto;
import com.team404.synco.workspace.service.WorkSpaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/workspace")
public class WorkSpaceController {
    private final WorkSpaceService workSpaceService;

    // 팀 워크스페이스 생성
    @PostMapping("/create")
    public ResponseDto<?> createWorkSpace(@ModelAttribute WorkSpaceCreateReqDto workSpaceCreateReqDto, @RequestHeader("X-Member-Seq")Long memberSeq){
        WorkSpaceResDto workSpaceResDto = workSpaceService.createTeamWorkSpace(workSpaceCreateReqDto, memberSeq);
        return ResponseDto.ok(workSpaceResDto, HttpStatus.OK);
    }
    // 워크스페이스 수정
    // 워크스페이스 삭제
    // 내 워크스페이스 목록 조회
    // 워크스페이스 초대
    // 채널 생성
    // 채널 수정
    // 채널 삭제
}
