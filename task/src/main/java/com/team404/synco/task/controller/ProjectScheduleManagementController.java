package com.team404.synco.task.controller;

import com.team404.synco.common.constant.dto.ResponseDto;
import com.team404.synco.task.dto.request.TaskCreateRequestDto;
import com.team404.synco.task.service.TeamScheduleManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/scheduleManagement")
public class ScheduleManagementController {

    private final TeamScheduleManagementService scheduleManagementService;

    /// 1. team space task 전체 조회
    @GetMapping("/task/{workSpaceSeq}")
    public ResponseEntity<?> getAllTeamsTask(@PathVariable long workSpaceSeq) {
        return ResponseEntity.status(HttpStatus.OK).body(scheduleManagementService.getAllTasksResponseDtoList(workSpaceSeq));
    }

    /// 2. 내 board 조회 + 내가 담당자로 등록된 task 조회
    @GetMapping("/board/{workSpaceSeq}")
    public ResponseEntity<?> getChannelBoard(@RequestHeader("X-Member-seq") long memberSeq, @PathVariable long workSpaceSeq) {
        return ResponseEntity.status(HttpStatus.OK).body(scheduleManagementService.fetchBoardsWithTasksByChannelMember(memberSeq, workSpaceSeq));
    }

    /// 3. task 등록
    @PostMapping("/task")
    public ResponseEntity<ResponseDto<?>> createTask(@RequestHeader("X-Member-seq") long memberSeq, @RequestBody TaskCreateRequestDto taskCreateRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.ok(scheduleManagementService.createTeamTask(memberSeq, taskCreateRequestDto), HttpStatus.CREATED));
    }

//    /// 4. board 등록
//    @PostMapping("/board")
//    public ResponseEntity<?> createBoard(@RequestHeader("X-Member-seq") long memberSeq, @PathVariable long workSpaceSeq) {
//        return ResponseEntity.created(scheduleManagementService.createBoard(memberSeq, workSpaceSeq));
//    }
}
