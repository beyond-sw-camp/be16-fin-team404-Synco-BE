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
import java.util.List;

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

    // 내 워크스페이스 목록 조회
    @GetMapping("/me")
    public ResponseEntity<ResponseDto<?>> myWorkSpaceList(@RequestHeader("X-Member-Seq") Long memberSeq) throws AccessDeniedException {
        List<WorkSpaceInfoResDto> myWorkSpaceListResDto = workSpaceService.findMyWorkSpaceList(memberSeq);
        return ResponseEntity.ok(ResponseDto.ok(myWorkSpaceListResDto, HttpStatus.OK));
    }

    // 워크스페이스 멤버 목록 조회
    @GetMapping("/{workSpaceSeq}/members")
    public ResponseEntity<ResponseDto<?>> getWorkSpaceMemberList(@PathVariable("workSpaceSeq") Long workSpaceSeq) throws AccessDeniedException {
        List<WorkSpaceMemberInfoResDto> workSpaceMemberListResDto = workSpaceService.findWorkSpaceMemberList(workSpaceSeq);
        return ResponseEntity.ok(ResponseDto.ok(workSpaceMemberListResDto, HttpStatus.OK));
    }

    // 내 워크스페이스 대시보드
    @GetMapping("/personal/{workSpaceSeq}")
    public ResponseEntity<ResponseDto<?>> personalDashBoardDetail(@PathVariable("workSpaceSeq") Long workSpaceSeq) throws AccessDeniedException {
        List<WorkSpaceInfoResDto> myWorkSpaceListResDto = workSpaceService.findMyWorkSpaceList(workSpaceSeq);
        return ResponseEntity.ok(ResponseDto.ok(myWorkSpaceListResDto, HttpStatus.OK));
    }

    // 팀 워크스페이스 대시보드
    @GetMapping("/teams/{workSpaceSeq}")
    public ResponseEntity<ResponseDto<?>> teamDashBoardDetail(@PathVariable("workSpaceSeq") Long workSpaceSeq) throws AccessDeniedException {
        TeamDashBoardResDto teamDashBoardResDto = workSpaceService.findTeamDashBoard(workSpaceSeq);
        return ResponseEntity.ok(ResponseDto.ok(teamDashBoardResDto, HttpStatus.OK));
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
    public ResponseEntity<ResponseDto<?>> deleteWorkSpace(@PathVariable("workSpaceSeq") Long workSpaceSeq,
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

    // 채널 삭제
}
