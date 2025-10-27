package com.team404.synco.task.controller;

import com.team404.synco.common.constant.dto.ResponseDto;
import com.team404.synco.task.dto.request.PersonalTaskCreateReqDto;
import com.team404.synco.task.dto.request.PersonalTaskUpdateReqDto;
import com.team404.synco.task.dto.request.TaskStatusUpdateReqDto;
import com.team404.synco.task.service.PersonalScheduleManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/scheduleManagement/personal")
public class PersonalScheduleManagementController {

    private final PersonalScheduleManagementService personalScheduleManagementService;

    // 개인 스케줄 Task 생성
    @PostMapping("/task/{workSpaceSeq}")
    public ResponseEntity<ResponseDto<?>> createPersonalTask(@RequestHeader("X-Member-Seq") Long memberSeq,
                                                             @PathVariable Long workSpaceSeq,
                                                             @Valid @RequestBody PersonalTaskCreateReqDto createReqDto) {
        Long taskSeq = personalScheduleManagementService.createPersonalTask(memberSeq, workSpaceSeq, createReqDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.ok(taskSeq, HttpStatus.CREATED));
    }

    // 개인 스케줄 Task 목록 조회 (상태별로 그룹화)
    @GetMapping("/tasks/{workSpaceSeq}")
    public ResponseEntity<ResponseDto<?>> getPersonalTasks(@RequestHeader("X-Member-Seq") Long memberSeq,
                                                           @PathVariable Long workSpaceSeq) {
        return ResponseEntity.ok(ResponseDto.ok(personalScheduleManagementService.getPersonalTasks(memberSeq, workSpaceSeq), HttpStatus.OK));
    }

    // 개인 스케줄 Task 상세 조회
    @GetMapping("/task/{taskSeq}")
    public ResponseEntity<ResponseDto<?>> getPersonalTask(@PathVariable Long taskSeq,
                                                          @RequestHeader("X-Member-Seq") Long memberSeq) {
        return ResponseEntity.ok(ResponseDto.ok(personalScheduleManagementService.getPersonalTask(taskSeq, memberSeq), HttpStatus.OK));
    }

    // 개인 스케줄 Task 수정
    @PatchMapping("/task/{taskSeq}")
    public ResponseEntity<ResponseDto<?>> updatePersonalTask(@PathVariable Long taskSeq,
                                                              @RequestHeader("X-Member-Seq") Long memberSeq,
                                                              @Valid @RequestBody PersonalTaskUpdateReqDto updateReqDto) {
        personalScheduleManagementService.updatePersonalTask(taskSeq, memberSeq, updateReqDto);
        return ResponseEntity.ok(ResponseDto.ok("개인 스케줄이 수정되었습니다.", HttpStatus.OK));
    }

    // 개인 스케줄 Task 상태 변경
    @PatchMapping("/task/{taskSeq}/status")
    public ResponseEntity<ResponseDto<?>> updatePersonalTaskStatus(@PathVariable Long taskSeq,
                                                                   @RequestHeader("X-Member-Seq") Long memberSeq,
                                                                   @Valid @RequestBody TaskStatusUpdateReqDto statusUpdateReqDto) {
        personalScheduleManagementService.updatePersonalTaskStatus(taskSeq, memberSeq, statusUpdateReqDto);
        return ResponseEntity.ok(ResponseDto.ok("Task 상태가 성공적으로 변경되었습니다.", HttpStatus.OK));
    }

    // 개인 스케줄 Task 삭제
    @DeleteMapping("/task/{taskSeq}")
    public ResponseEntity<ResponseDto<?>> deletePersonalTask(@PathVariable Long taskSeq,
                                                              @RequestHeader("X-Member-Seq") Long memberSeq) {
        personalScheduleManagementService.deletePersonalTask(taskSeq, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok("개인 스케줄이 삭제되었습니다.", HttpStatus.OK));
    }
}
