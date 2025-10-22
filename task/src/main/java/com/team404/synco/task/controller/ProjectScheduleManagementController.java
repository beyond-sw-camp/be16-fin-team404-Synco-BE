package com.team404.synco.task.controller;

import com.team404.synco.common.constant.dto.ResponseDto;
import com.team404.synco.task.dto.request.BoardCreateReqDto;
import com.team404.synco.task.dto.request.TaskCreateReqDto;
import com.team404.synco.task.service.ProjectScheduleManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/scheduleManagement/project")
public class ProjectScheduleManagementController {

    private final ProjectScheduleManagementService scheduleManagementService;

    /// 1. project space task 전체 조회
    @GetMapping("/task/{workSpaceSeq}")
    public ResponseEntity<ResponseDto<?>> getAllTeamsTask(@PathVariable long workSpaceSeq) {
        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.ok(scheduleManagementService.getAllTasksResponseDtoList(workSpaceSeq), HttpStatus.OK));
    }

    /// 2. project space 내 board 조회 + 내가 담당자로 등록된 task 조회
    @GetMapping("/board/{workSpaceSeq}")
    public ResponseEntity<ResponseDto<?>> getChannelBoard(@RequestHeader("X-Member-seq") long memberSeq, @PathVariable long workSpaceSeq) {
        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.ok(scheduleManagementService.fetchBoardsWithTasksByChannelMember(memberSeq, workSpaceSeq), HttpStatus.OK));
    }

    /// 4. project space 내 board 등록
    @PostMapping("/board")
    public ResponseEntity<ResponseDto<?>> createProjectBoard(@RequestBody BoardCreateReqDto boardCreateReqDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.ok(scheduleManagementService.createProjectBoard(boardCreateReqDto), HttpStatus.CREATED));
    }

    /// 3. project space task 등록
    @PostMapping("/task")
    public ResponseEntity<ResponseDto<?>> createProjectTask(@RequestHeader("X-Member-seq") long memberSeq, @RequestBody TaskCreateReqDto taskCreateReqDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.ok(scheduleManagementService.createProjectTaskAfterAuthorityCheck(memberSeq, taskCreateReqDto), HttpStatus.CREATED));
    }

    ///  4. workspace 참여 목록조회
    @GetMapping("/memberList/{workSpaceSeq}")
    public ResponseEntity<ResponseDto<?>> getWorkspaceMembers(@PathVariable long workSpaceSeq) {
        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.ok(scheduleManagementService.getWorkspaceMemberList(workSpaceSeq), HttpStatus.OK));
    }
}
