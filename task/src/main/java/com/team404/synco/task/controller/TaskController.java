package com.team404.synco.task.controller;

import com.team404.synco.common.constant.dto.DelegateSuperAuthorityReqDto;
import com.team404.synco.common.constant.dto.ResponseDto;
import com.team404.synco.task.dto.TaskChannelMemberCreateReqDto;
import com.team404.synco.task.service.TaskService;
import com.team404.synco.virtualmeeting.dto.ChannelInviteReqDto;
import com.team404.synco.virtualmeeting.dto.GrantAuthorityReqDto;
import com.team404.synco.virtualmeeting.dto.KickMemberFromWorkSpaceReqDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/task")
public class TaskController {
    private final TaskService taskService;
    @PostMapping("/create")
    public ResponseEntity<ResponseDto<?>> createTask(@RequestBody TaskChannelMemberCreateReqDto taskChannelMemberCreateReqDto){
        taskService.createTaskChannel(taskChannelMemberCreateReqDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.ok("OK", HttpStatus.CREATED));
    };

    // 채널에 멤버 추가
    @PostMapping("/addMember")
    public ResponseEntity<ResponseDto<?>> addMember(@RequestBody ChannelInviteReqDto channelInviteReqDto){
        Long id = taskService.addMemberToChannel(channelInviteReqDto);
        return ResponseEntity.ok(ResponseDto.ok(id, HttpStatus.OK));
    }

    // 채널 권한 설정
    @PatchMapping("/changeChannelAuthority")
    public ResponseEntity<ResponseDto<?>> changeChannelAuthority(@RequestBody GrantAuthorityReqDto grantAuthorityReqDto,
                                                                 @RequestHeader("X-Member-Seq")Long memberSeq) throws AccessDeniedException
    {
        taskService.grantToMember(grantAuthorityReqDto, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok("해당 사용자의 권한을 변경했습니다.", HttpStatus.OK));
    }

    // 채널 SUPER 권한 위임
    @PostMapping("/delegateSuperAuthority")
    public ResponseEntity<ResponseDto<?>> delegateSuperAuthority(@RequestBody DelegateSuperAuthorityReqDto delegateSuperAuthorityReqDto,
                                                                 @RequestHeader("X-Member-Seq") Long memberSeq) throws AccessDeniedException
    {
        taskService.delegateSuperAuthority(delegateSuperAuthorityReqDto, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok("채널의 SUPER 권한 사용자가 변경되었습니다.", HttpStatus.OK));
    }

    // 채널 리스트 조회
    @GetMapping("/channels/{workSpaceSeq}")
    public ResponseEntity<ResponseDto<?>> getChannelList(@PathVariable("workSpaceSeq") Long workSpaceSeq)
    {
        return ResponseEntity.ok(ResponseDto.ok(taskService.findTaskChannelMember(workSpaceSeq), HttpStatus.OK));
    }

    // 내 프로젝트 목록
    @GetMapping("/memberList")
    public List<Long> findMyWorkSpaceList(@RequestHeader("X-Member-Seq") Long memberSeq){
        return taskService.myWorkSpaceList(memberSeq);
    }

    // 프로젝트 멤버 목록
    @GetMapping("/{workSpaceSeq}/members")
    public List<Long> findWorkSpaceMemberList(@PathVariable("workSpaceSeq") Long workSpaceSeq){
        return taskService.workSpaceMemberList(workSpaceSeq);
    }

    // 팀 테스크 전체 삭제
    @DeleteMapping("/{workSpaceSeq}")
    public void deleteTeamTaskChannel(@PathVariable("workSpaceSeq") Long workSpaceSeq){
        taskService.deleteAllTask(workSpaceSeq);
    }

    // 프로젝트 탈퇴
    @DeleteMapping("/leave/{workSpaceSeq}")
    public void leaveWorkSpace(@PathVariable("workSpaceSeq")Long workSpaceSeq, @RequestHeader("X-Member-Seq") Long memberSeq){
        taskService.deleteMemberFromWorkSpace(workSpaceSeq, memberSeq);
    }

    // 프로젝트 강제탈퇴
    @DeleteMapping("/kick")
    public void kickFromWorkSpace(@RequestBody KickMemberFromWorkSpaceReqDto kickMemberFromWorkSpaceReqDto){
        Long workSpaceSeq = kickMemberFromWorkSpaceReqDto.getWorkSpaceSeq();
        Long memberSeq = kickMemberFromWorkSpaceReqDto.getMemberSeq();
        taskService.deleteMemberFromWorkSpace(workSpaceSeq, memberSeq);
    }
}
