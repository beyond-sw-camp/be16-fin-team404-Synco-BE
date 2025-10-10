package com.team404.synco.task.controller;

import com.team404.synco.common.constant.dto.ResponseDto;
import com.team404.synco.task.dto.TaskChannelMemberCreateReqDto;
import com.team404.synco.task.service.TaskService;
import com.team404.synco.virtualmeeting.dto.ChannelInviteReqDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // 팀 테스크 전체 삭제
    @DeleteMapping("/{workSpaceSeq}")
    public void deleteTeamTaskChannel(@PathVariable Long workSpaceSeq){
        taskService.deleteAllTask(workSpaceSeq);
    }
}
