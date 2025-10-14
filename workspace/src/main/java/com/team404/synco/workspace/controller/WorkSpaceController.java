package com.team404.synco.workspace.controller;

import com.team404.synco.common.dto.ResponseDto;
import com.team404.synco.workspace.dto.*;
import com.team404.synco.workspace.service.WorkSpaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/workspace")
public class WorkSpaceController {
    private final WorkSpaceService workSpaceService;

    // 프로젝트 워크스페이스 생성
    @PostMapping("/create")
    public ResponseEntity<ResponseDto<?>> createWorkSpace(@ModelAttribute TeamWorkSpaceCreateReqDto teamWorkSpaceCreateReqDto,
                                                          @RequestHeader("X-Member-Seq") Long memberSeq) {
        WorkSpaceResDto workSpaceResDto = workSpaceService.createTeamWorkSpace(teamWorkSpaceCreateReqDto, memberSeq);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.ok(workSpaceResDto, HttpStatus.CREATED));
    }

    // 프로젝트 워크스페이스 수정
    @PatchMapping("/edit")
    public ResponseEntity<ResponseDto<?>> editWorkSpace(@ModelAttribute TeamWorkSpaceEditReqDto teamWorkSpaceEditReqDto,
                                                        @RequestHeader("X-Member-Seq") Long memberSeq) throws AccessDeniedException {
        WorkSpaceResDto workSpaceResDto = workSpaceService.editWorkSpace(teamWorkSpaceEditReqDto, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok(workSpaceResDto, HttpStatus.OK));
    }

    // 프로젝트 워크스페이스 삭제
    @DeleteMapping("/{workSpaceSeq}")
    public ResponseEntity<ResponseDto<?>> deleteWorkSpace(@PathVariable Long workSpaceSeq,
                                                          @RequestHeader("X-Member-Seq") Long memberSeq) throws Exception {
        workSpaceService.deleteWorkSpace(workSpaceSeq, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok("팀 워크스페이스가 성공적으로 삭제되었습니다.", HttpStatus.OK));
    }
    //프로젝트 워크스페이스 SUPER 권한 위임
    @PostMapping("/delegateSuperAuthority")
    public ResponseEntity<ResponseDto<?>> delegateSuperAuthority(@RequestBody DelegateSuperAuthorityReqDto delegateSuperAuthorityReqDto,
                                                                 @RequestHeader("X-Member-Seq") Long memberSeq) throws AccessDeniedException {
        workSpaceService.delegateSuperAuthority(delegateSuperAuthorityReqDto, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok("워크스페이스의 SUPER 권한 사용자가 변경되었습니다.", HttpStatus.OK));
    }

    // 워크스페이스 초대
    @PostMapping("/invite")
    public ResponseEntity<ResponseDto<?>> inviteWorkSpace(@RequestBody ChannelInviteReqDto channelInviteReqDto,
                                                                 @RequestHeader("X-Member-Seq") Long memberSeq) throws AccessDeniedException {
        workSpaceService.inviteWorkSpace(channelInviteReqDto, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok("성공적으로 워크스페이스에 초대되었습니다.", HttpStatus.OK));
    }

    // 내 워크스페이스 목록 조회

    // 채널 삭제
}
