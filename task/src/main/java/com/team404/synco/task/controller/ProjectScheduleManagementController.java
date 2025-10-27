package com.team404.synco.task.controller;

import com.team404.synco.common.constant.dto.ResponseDto;
import com.team404.synco.task.dto.request.BoardCreateReqDto;
import com.team404.synco.task.dto.request.TaskCreateReqDto;
import com.team404.synco.task.dto.request.TaskUpdateReqDto;
import com.team404.synco.task.dto.request.TaskStatusUpdateReqDto;
import com.team404.synco.task.dto.request.BoardChangeReqDto;
import com.team404.synco.task.dto.request.BoardUpdateReqDto;
import com.team404.synco.task.dto.request.BoardOrderUpdateReqDto;
import com.team404.synco.task.service.ProjectScheduleManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/scheduleManagement/project")
public class    ProjectScheduleManagementController {

    private final ProjectScheduleManagementService scheduleManagementService;

    /// 1. project space task 전체 조회 (사용자별 필터링 가능)
    @GetMapping("/tasks/{workSpaceSeq}")
    public ResponseEntity<ResponseDto<?>> getAllTeamsTask(@RequestHeader("X-Member-seq") long memberSeq,
                                                          @PathVariable long workSpaceSeq,
                                                          @RequestParam(required = false) Long assigneeMemberSeq) {
        return ResponseEntity.ok(ResponseDto.ok(scheduleManagementService.getAllTasksResponseDtoList(workSpaceSeq, assigneeMemberSeq, memberSeq), HttpStatus.OK));
    }

    /// 2. project space 내 board 조회 + 내가 담당자로 등록된 task 조회
    @GetMapping("/boards/{workSpaceSeq}")
    public ResponseEntity<ResponseDto<?>> getChannelBoard(@RequestHeader("X-Member-seq") long memberSeq,
                                                          @PathVariable long workSpaceSeq) {
        return ResponseEntity.ok(ResponseDto.ok(scheduleManagementService.fetchBoardsWithTasksByChannelMember(memberSeq, workSpaceSeq), HttpStatus.OK));
    }

    /// 4. project space 내 board 등록 (요청자 검증)
    @PostMapping("/board")
    public ResponseEntity<ResponseDto<?>> createProjectBoard(@RequestHeader("X-Member-seq") long memberSeq,
                                                             @RequestBody BoardCreateReqDto boardCreateReqDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDto.ok(scheduleManagementService.createProjectBoard(memberSeq, boardCreateReqDto), HttpStatus.CREATED));
    }

    /// 3. project space task 등록
    @PostMapping("/task")
    public ResponseEntity<ResponseDto<?>> createProjectTask(@RequestHeader("X-Member-seq") long memberSeq,
                                                            @RequestBody TaskCreateReqDto taskCreateReqDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDto.ok(scheduleManagementService.createProjectTaskAfterAuthorityCheck(memberSeq, taskCreateReqDto), HttpStatus.CREATED));
    }

    ///  4. workspace 참여 목록조회
    @GetMapping("/memberList/{workSpaceSeq}")
    public ResponseEntity<ResponseDto<?>> getWorkspaceMembers(@RequestHeader("X-Member-seq") long memberSeq,
                                                              @PathVariable long workSpaceSeq) {
        return ResponseEntity.ok(ResponseDto.ok(scheduleManagementService.getWorkspaceMemberList(memberSeq, workSpaceSeq), HttpStatus.OK));
    }

    /// 6. 내가 담당자인 보드가 없는 Task들 조회 (개인화면 초기용)
    @GetMapping("/myTasks/{workSpaceSeq}")
    public ResponseEntity<ResponseDto<?>> getMyTasksWithoutBoard(@RequestHeader("X-Member-seq") long memberSeq,
                                                                 @PathVariable long workSpaceSeq) {
        return ResponseEntity.ok(ResponseDto.ok(scheduleManagementService.fetchMyTasksWithoutBoard(memberSeq, workSpaceSeq), HttpStatus.OK));
    }

    /// 7. Task 상태만 변경 (칸반보드 드래그 앤 드롭용)
    @PatchMapping("/task/{taskSeq}/status")
    public ResponseEntity<ResponseDto<?>> updateTaskStatusOnly(@RequestHeader("X-Member-seq") long memberSeq,
                                                               @PathVariable long taskSeq,
                                                               @RequestBody TaskStatusUpdateReqDto taskStatusUpdateReqDto) {
        scheduleManagementService.updateTaskStatusOnly(taskSeq, taskStatusUpdateReqDto, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok("Task 상태가 성공적으로 변경되었습니다.", HttpStatus.OK));
    }

    /// 8. Task 전체 수정 (상세 페이지용)
    @PatchMapping("/task/{taskSeq}")
    public ResponseEntity<ResponseDto<?>> updateTaskFull(@RequestHeader("X-Member-seq") long memberSeq,
                                                         @PathVariable long taskSeq,
                                                         @RequestBody TaskUpdateReqDto taskUpdateReqDto) {
        scheduleManagementService.updateTaskFull(taskSeq, taskUpdateReqDto, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok("Task가 성공적으로 수정되었습니다.", HttpStatus.OK));
    }

    /// 9. Task 보드 변경 (개인화면용)
    @PatchMapping("/task/{taskSeq}/board")
    public ResponseEntity<ResponseDto<?>> updateTaskBoard(@RequestHeader("X-Member-seq") long memberSeq,
                                                          @PathVariable long taskSeq,
                                                          @RequestBody BoardChangeReqDto boardChangeReqDto) {
        scheduleManagementService.updateTaskBoard(taskSeq, boardChangeReqDto, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok("Task 보드가 성공적으로 변경되었습니다.", HttpStatus.OK));
    }

    /// 10. Board 수정 (개인화면용)
    @PatchMapping("/board/{boardSeq}")
    public ResponseEntity<ResponseDto<?>> updateBoard(@RequestHeader("X-Member-seq") long memberSeq,
                                                      @PathVariable long boardSeq,
                                                      @RequestBody BoardUpdateReqDto boardUpdateReqDto) {
        scheduleManagementService.updateBoard(boardSeq, boardUpdateReqDto, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok("Board가 성공적으로 수정되었습니다.", HttpStatus.OK));
    }

    /// 11. Task 상세 조회
    @GetMapping("/task/{taskSeq}")
    public ResponseEntity<ResponseDto<?>> getTaskDetail(@RequestHeader("X-Member-seq") long memberSeq,
                                                        @PathVariable long taskSeq) {
        return ResponseEntity.ok(ResponseDto.ok(scheduleManagementService.getTaskDetail(taskSeq, memberSeq), HttpStatus.OK));
    }

    /// 12. Board 상세 조회
    @GetMapping("/board/{boardSeq}")
    public ResponseEntity<ResponseDto<?>> getBoardDetail(@RequestHeader("X-Member-seq") long memberSeq,
                                                         @PathVariable long boardSeq) {
        return ResponseEntity.ok(ResponseDto.ok(scheduleManagementService.getBoardDetail(boardSeq, memberSeq), HttpStatus.OK));
    }

    /// 13. Task 삭제 (SUPER/MANAGER만 가능)
    @DeleteMapping("/task/{taskSeq}")
    public ResponseEntity<ResponseDto<?>> deleteTask(@RequestHeader("X-Member-seq") long memberSeq,
                                                     @PathVariable long taskSeq) {
        scheduleManagementService.deleteTask(taskSeq, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok("Task가 성공적으로 삭제되었습니다.", HttpStatus.OK));
    }

    /// 14. Board 삭제 (개인화면용)
    @DeleteMapping("/board/{boardSeq}")
    public ResponseEntity<ResponseDto<?>> deleteBoard(@RequestHeader("X-Member-seq") long memberSeq,
                                                      @PathVariable long boardSeq) {
        scheduleManagementService.deleteBoard(boardSeq, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok("Board가 성공적으로 삭제되었습니다.", HttpStatus.OK));
    }

    /// 15. Board 순서 변경 (개인화면용 - 드래그 앤 드롭)
    @PatchMapping("/boards/orders")
    public ResponseEntity<ResponseDto<?>> updateBoardOrders(@RequestHeader("X-Member-seq") long memberSeq,
                                                           @RequestBody List<BoardOrderUpdateReqDto> boardOrderUpdates) {
        scheduleManagementService.updateBoardOrders(boardOrderUpdates, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok("Board 순서가 성공적으로 변경되었습니다.", HttpStatus.OK));
    }
}
