package com.team404.synco.task.controller;

import com.team404.synco.common.constant.dto.DelegateSuperAuthorityReqDto;
import com.team404.synco.common.constant.dto.ResponseDto;
import com.team404.synco.task.dto.TaskChannelMemberCreateReqDto;
import com.team404.synco.task.service.TaskService;
import com.team404.synco.virtualmeeting.dto.ChannelInviteReqDto;
import com.team404.synco.virtualmeeting.dto.GrantAuthorityReqDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

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

    // 팀 테스크 전체 삭제
    @DeleteMapping("/{workSpaceSeq}")
    public void deleteTeamTaskChannel(@PathVariable Long workSpaceSeq){
        taskService.deleteAllTask(workSpaceSeq);
    }
}
