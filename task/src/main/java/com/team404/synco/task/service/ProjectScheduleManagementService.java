package com.team404.synco.task.service;

import com.team404.synco.common.constant.Authority;
import com.team404.synco.common.component.MemberRedisComponent;
import com.team404.synco.task.constant.TaskStatus;
import com.team404.synco.task.dto.request.BoardCreateReqDto;
import com.team404.synco.task.dto.request.TaskCreateReqDto;
import com.team404.synco.task.dto.request.TaskStatusUpdateReqDto;
import com.team404.synco.task.dto.request.TaskUpdateReqDto;
import com.team404.synco.task.dto.request.BoardChangeReqDto;
import com.team404.synco.task.dto.request.BoardUpdateReqDto;
import com.team404.synco.task.dto.request.BoardOrderUpdateReqDto;
import com.team404.synco.task.dto.response.BoardResDto;
import com.team404.synco.task.dto.response.TasksResDto;
import com.team404.synco.task.dto.response.WorkspaceMemberDto;
import com.team404.synco.task.dto.response.TaskDetailResDto;
import com.team404.synco.task.dto.response.BoardDetailResDto;
import com.team404.synco.task.entity.Board;
import com.team404.synco.task.entity.ScheduleManagementChannelMember;
import com.team404.synco.task.entity.Task;
import com.team404.synco.task.repository.BoardRepository;
import com.team404.synco.task.repository.ScheduleManagementChannelMemberRepository;
import com.team404.synco.task.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectScheduleManagementService {
    private final BoardRepository boardRepository;
    private final TaskRepository taskRepository;
    private final MemberRedisComponent memberRedisComponent;
    private final ScheduleManagementChannelMemberRepository scheduleManagementChannelMemberRepository;

    // 워크스페이스의 모든 Task를 상태별로 그룹화해서 반환 (팀일정 화면용)
    @Transactional(readOnly = true)
    public List<TasksResDto> getAllTasksResponseDtoList(final long workSpaceSeq, final Long assigneeMemberSeq, final long requesterMemberSeq) {
        // 요청자 워크스페이스 참여 검증
        scheduleManagementChannelMemberRepository
                .findByMemberSeqAndWorkSpaceSeq(requesterMemberSeq, workSpaceSeq)
                .orElseThrow(() -> new EntityNotFoundException("해당 워크스페이스에 참여하지 않은 사용자입니다."));

        List<Task> tasks;
        
        if (assigneeMemberSeq != null) {
            // 특정 사용자 담당 업무만 필터링
            tasks = taskRepository.findTasksByAssigneeAndWorkSpace(assigneeMemberSeq, workSpaceSeq);
        } else {
            // 전체 업무 조회
            tasks = taskRepository.findAllByWorkSpaceSeqOrderByCreatedAsc(workSpaceSeq);
        }
        
        final Map<TaskStatus, List<Task>> groupedTasks = tasks.stream()
                .collect(Collectors.groupingBy(Task::getTaskStatus));
        
        return groupedTasks.entrySet().stream()
                .map(entry -> TasksResDto.fromEntity(entry.getKey(), entry.getValue()))
                .toList();
    }

    // Task 생성 
    @Transactional
    public long createProjectTaskAfterAuthorityCheck(final long memberSeq, final TaskCreateReqDto taskCreateReqDto) {
        final ScheduleManagementChannelMember picMember = scheduleManagementChannelMemberRepository
                .findById(taskCreateReqDto.getPicMemberSeq()).orElseThrow(() -> new EntityNotFoundException("일정관리 채널 업무 담당자 회원을 찾을 수 없습니다."));
        final ScheduleManagementChannelMember createMember = scheduleManagementChannelMemberRepository.findByMemberSeqAndWorkSpaceSeq(memberSeq, picMember.getWorkSpaceSeq())
                .orElseThrow(() -> new EntityNotFoundException("일정관리 채널 생성자 회원을 찾을 수 없습니다."));
        if (createMember.getAuthority() == Authority.PARTICIPANT) {
            throw new ForbiddenException("권한이 유효하지 않습니다.");
        }
        Optional<Board> board = Optional.empty();
        if(taskCreateReqDto.getBoardSeq() != null) {
            board = boardRepository.findById(taskCreateReqDto.getBoardSeq());
            if (board.isEmpty()) {
                throw new EntityNotFoundException("일정관리 채널 보드를 찾을 수 없습니다.");
            }
        }
        return taskRepository.save(taskCreateReqDto.toEntity(picMember, board)).getTaskSeq();
    }

    // 특정 멤버가 생성한 보드들과 해당 보드의 Task들을 반환 (개인일정 화면용)
    @Transactional(readOnly = true)
    public List<BoardResDto> fetchBoardsWithTasksByChannelMember(final long memberSeq, final long workSpaceSeq) {
        List<Board> boardList = boardRepository.findAllByMemberAndWorkSpace(memberSeq, workSpaceSeq);
        return boardList.stream()
            .map(BoardResDto::fromEntity)
            .toList();
    }

    // 내가 담당자인 보드가 없는 Task들 조회
    @Transactional(readOnly = true)
    public List<TasksResDto.TaskResDto> fetchMyTasksWithoutBoard(final long memberSeq, final long workSpaceSeq) {
        // memberSeq와 workSpaceSeq로 ScheduleManagementChannelMemberSeq 찾기
        ScheduleManagementChannelMember member = scheduleManagementChannelMemberRepository
                .findByMemberSeqAndWorkSpaceSeq(memberSeq, workSpaceSeq)
                .orElseThrow(() -> new EntityNotFoundException("해당 워크스페이스의 일정관리 채널 멤버를 찾을 수 없습니다."));
        
        List<Task> myTasksWithoutBoard = taskRepository.findMyTasksWithoutBoard(member.getScheduleManagementChannelMemberSeq());
        return myTasksWithoutBoard.stream()
                .map(TasksResDto.TaskResDto::fromEntity)
                .toList();
    }

    // Task 상태만 변경 (칸반보드 드래그 앤 드롭용)
    @Transactional
    public void updateTaskStatusOnly(final long taskSeq, final TaskStatusUpdateReqDto taskStatusUpdateReqDto, final long memberSeq) {
        Task task = taskRepository.findById(taskSeq)
                .orElseThrow(() -> new EntityNotFoundException("Task를 찾을 수 없습니다."));
        
        // 권한 검증: SUPER/MANAGER는 모든 Task 변경 가능, 또는 본인이 담당자면 변경 가능
        ScheduleManagementChannelMember requester = scheduleManagementChannelMemberRepository
                .findByMemberSeqAndWorkSpaceSeq(memberSeq, task.getPicMemberSeq().getWorkSpaceSeq())
                .orElseThrow(() -> new EntityNotFoundException("해당 워크스페이스의 일정관리 채널 멤버를 찾을 수 없습니다."));
        
        // PARTICIPANT는 본인이 담당자인 Task만 상태 변경 가능
        if (requester.getAuthority() == Authority.PARTICIPANT && 
            task.getPicMemberSeq().getMemberSeq() != memberSeq) {
            throw new ForbiddenException("PARTICIPANT는 본인이 담당자인 Task만 상태를 변경할 수 있습니다.");
        }
        
        task.updateTaskStatus(taskStatusUpdateReqDto.getTaskStatus());
    }

    // Task 전체 수정
    @Transactional
    public void updateTaskFull(final long taskSeq, final TaskUpdateReqDto taskUpdateReqDto, final long memberSeq) {
        Task task = taskRepository.findById(taskSeq)
                .orElseThrow(() -> new EntityNotFoundException("Task를 찾을 수 없습니다."));
        
        // 권한 검증
        ScheduleManagementChannelMember requester = scheduleManagementChannelMemberRepository
                .findByMemberSeqAndWorkSpaceSeq(memberSeq, task.getPicMemberSeq().getWorkSpaceSeq())
                .orElseThrow(() -> new EntityNotFoundException("해당 워크스페이스의 일정관리 채널 멤버를 찾을 수 없습니다."));

        // PARTICIPANT는 본인이 담당자인 Task만 수정 가능
        if (requester.getAuthority() == Authority.PARTICIPANT &&
            task.getPicMemberSeq().getMemberSeq() != memberSeq) {
            throw new ForbiddenException("PARTICIPANT는 본인이 담당자인 Task만 수정할 수 있습니다.");
        }

        // PARTICIPANT가 본인 Task 수정 시에는 상태와 보드만 수정 가능
        if (requester.getAuthority() == Authority.PARTICIPANT) {
            // 상태만 변경
            task.updateTaskStatus(taskUpdateReqDto.getTaskStatus());
            
            // 보드만 변경
            Optional<Board> board = Optional.empty();
            if (taskUpdateReqDto.getBoardSeq() != null) {
                board = boardRepository.findById(taskUpdateReqDto.getBoardSeq());
            }
            task.updateBoard(board.orElse(null));
        } else {
            // SUPER/MANAGER는 전체 수정 가능
            final ScheduleManagementChannelMember picMember = scheduleManagementChannelMemberRepository
                    .findById(taskUpdateReqDto.getPicMemberSeq())
                    .orElseThrow(() -> new EntityNotFoundException("담당자를 찾을 수 없습니다."));
            
            Optional<Board> board = Optional.empty();
            if (taskUpdateReqDto.getBoardSeq() != null) {
                board = boardRepository.findById(taskUpdateReqDto.getBoardSeq());
            }

            task.updateTask(taskUpdateReqDto, picMember, board.orElse(null));
        }
    }

    // Task 보드 변경
    @Transactional
    public void updateTaskBoard(final long taskSeq, final BoardChangeReqDto boardChangeReqDto, final long memberSeq) {
        Task task = taskRepository.findById(taskSeq)
                .orElseThrow(() -> new EntityNotFoundException("Task를 찾을 수 없습니다."));
        
        // 로그인한 회원이 Task 담당자인지 검증
        if (task.getPicMemberSeq().getMemberSeq() != memberSeq) {
            throw new ForbiddenException("해당 Task의 담당자가 아닙니다.");
        }
        
        Optional<Board> board = Optional.empty();
        if (boardChangeReqDto.getBoardSeq() != null) {
            board = boardRepository.findById(boardChangeReqDto.getBoardSeq());
        }

        task.updateBoard(board.orElse(null));
    }

    // 보드 생성 (요청자 검증: DTO의 채널멤버가 요청자와 같은 워크스페이스/본인인지 확인)
    @Transactional
    public long createProjectBoard(final long requesterMemberSeq, final BoardCreateReqDto boardCreateReqDto) {
        final ScheduleManagementChannelMember scheduleManagementChannelMember = scheduleManagementChannelMemberRepository
                .findById(boardCreateReqDto.getScheduleManagementChannelMemberSeq())
                .orElseThrow(() -> new EntityNotFoundException("일정관리 채널 회원을 찾을수 없습니다."));

        // 요청자와 동일 워크스페이스 참여 여부 검증
        scheduleManagementChannelMemberRepository
                .findByMemberSeqAndWorkSpaceSeq(requesterMemberSeq, scheduleManagementChannelMember.getWorkSpaceSeq())
                .orElseThrow(() -> new EntityNotFoundException("해당 워크스페이스에 참여하지 않은 사용자입니다."));

        long maxOrder = boardRepository.findMaxOrderByMemberAndWorkSpace(
                scheduleManagementChannelMember.getMemberSeq(), 
                scheduleManagementChannelMember.getWorkSpaceSeq())
                .orElse(0L);

        Board board = boardCreateReqDto.toEntity(scheduleManagementChannelMember, maxOrder + 1);

        return boardRepository.save(board).getBoardSeq();
    }

    // Board 수정 (개인화면용)
    @Transactional
    public void updateBoard(final long boardSeq, final BoardUpdateReqDto boardUpdateReqDto, final long memberSeq) {
        Board board = boardRepository.findById(boardSeq)
                .orElseThrow(() -> new EntityNotFoundException("Board를 찾을 수 없습니다."));
        
        // 로그인한 회원이 Board 생성자인지 검증
        if (board.getScheduleManagementChannelMember().getMemberSeq() != memberSeq) {
            throw new ForbiddenException("해당 Board의 생성자가 아닙니다.");
        }
        
        board.updateBoard(boardUpdateReqDto);
    }

    // 워크스페이스 멤버 목록 조회
    @Transactional(readOnly = true)
    public List<WorkspaceMemberDto> getWorkspaceMemberList(final long memberSeq, final long workSpaceSeq) {
        // 요청한 사용자가 해당 워크스페이스에 참여했는지 검증
        scheduleManagementChannelMemberRepository
                .findByMemberSeqAndWorkSpaceSeq(memberSeq, workSpaceSeq)
                .orElseThrow(() -> new EntityNotFoundException("해당 워크스페이스에 참여하지 않은 사용자입니다."));

        List<ScheduleManagementChannelMember> memberList = scheduleManagementChannelMemberRepository
                .findAllByWorkSpaceSeq(workSpaceSeq).orElseThrow(() -> new EntityNotFoundException("조회되는 워크스페이스 목록이 없습니다."));

        return memberList.stream()
                .map(member -> WorkspaceMemberDto.fromEntity(
                        member,
                        memberRedisComponent.getMemberName(member.getMemberSeq()),
                        memberRedisComponent.getMemberProfileUrl(member.getMemberSeq())
                ))
                .toList();
    }

    // Task 상세 조회 (요청자 워크스페이스 참여 검증)
    @Transactional(readOnly = true)
    public TaskDetailResDto getTaskDetail(final long taskSeq, final long requesterMemberSeq) {
        Task task = taskRepository.findById(taskSeq)
                .orElseThrow(() -> new EntityNotFoundException("Task를 찾을 수 없습니다."));
        // 요청자 참여 검증
        scheduleManagementChannelMemberRepository
                .findByMemberSeqAndWorkSpaceSeq(requesterMemberSeq, task.getPicMemberSeq().getWorkSpaceSeq())
                .orElseThrow(() -> new EntityNotFoundException("해당 워크스페이스에 참여하지 않은 사용자입니다."));
        return TaskDetailResDto.fromEntity(task);
    }

    // Board 상세 조회 (요청자 워크스페이스 참여 검증)
    @Transactional(readOnly = true)
    public BoardDetailResDto getBoardDetail(final long boardSeq, final long requesterMemberSeq) {
        Board board = boardRepository.findById(boardSeq)
                .orElseThrow(() -> new EntityNotFoundException("Board를 찾을 수 없습니다."));
        // 요청자 참여 검증
        scheduleManagementChannelMemberRepository
                .findByMemberSeqAndWorkSpaceSeq(requesterMemberSeq, board.getScheduleManagementChannelMember().getWorkSpaceSeq())
                .orElseThrow(() -> new EntityNotFoundException("해당 워크스페이스에 참여하지 않은 사용자입니다."));
        return BoardDetailResDto.fromEntity(board);
    }

    // Task 삭제 (SUPER/MANAGER만 가능)
    @Transactional
    public void deleteTask(final long taskSeq, final long memberSeq) {
        Task task = taskRepository.findById(taskSeq)
                .orElseThrow(() -> new EntityNotFoundException("Task를 찾을 수 없습니다."));
        
        // 권한 검증: SUPER/MANAGER만 삭제 가능
        ScheduleManagementChannelMember requester = scheduleManagementChannelMemberRepository
                .findByMemberSeqAndWorkSpaceSeq(memberSeq, task.getPicMemberSeq().getWorkSpaceSeq())
                .orElseThrow(() -> new EntityNotFoundException("해당 워크스페이스의 일정관리 채널 멤버를 찾을 수 없습니다."));
        
        if (requester.getAuthority() != Authority.SUPER && requester.getAuthority() != Authority.MANAGER) {
            throw new ForbiddenException("SUPER/MANAGER만 Task를 삭제할 수 있습니다.");
        }
        
        taskRepository.delete(task);
    }

    // Board 삭제 (개인화면용 - 본인이 생성한 Board만 삭제 가능)
    @Transactional
    public void deleteBoard(final long boardSeq, final long memberSeq) {
        Board board = boardRepository.findById(boardSeq)
                .orElseThrow(() -> new EntityNotFoundException("Board를 찾을 수 없습니다."));
        
        // 로그인한 회원이 Board 생성자인지 검증
        if (board.getScheduleManagementChannelMember().getMemberSeq() != memberSeq) {
            throw new ForbiddenException("해당 Board의 생성자가 아닙니다.");
        }
        
        long deletedOrder = board.getOrders();
        long boardMemberSeq = board.getScheduleManagementChannelMember().getMemberSeq();
        long workSpaceSeq = board.getScheduleManagementChannelMember().getWorkSpaceSeq();
        
        // 해당 Board에 속한 모든 Task들의 board를 null로 변경
        List<Task> tasksInBoard = board.getTaskList();
        for (Task task : tasksInBoard) {
            task.updateBoard(null);
        }
        
        // 보드 삭제
        boardRepository.delete(board);
        
        // 삭제된 보드보다 큰 orders를 가진 보드들의 orders를 1씩 감소
        List<Board> boardsToUpdate = boardRepository.findBoardsWithOrdersGreaterThan(
            boardMemberSeq, workSpaceSeq, deletedOrder);
        
        for (Board boardToUpdate : boardsToUpdate) {
            boardToUpdate.updateOrders(boardToUpdate.getOrders() - 1);
        }
    }

    // 보드 순서 변경
    @Transactional
    public void updateBoardOrders(List<BoardOrderUpdateReqDto> boardOrderUpdates, long memberSeq) {
        for (BoardOrderUpdateReqDto update : boardOrderUpdates) {
            Board board = boardRepository.findById(update.getBoardSeq())
                    .orElseThrow(() -> new EntityNotFoundException("보드를 찾을 수 없습니다."));
            
            // 권한 검증
            if (board.getScheduleManagementChannelMember().getMemberSeq() != memberSeq) {
                throw new ForbiddenException("본인이 생성한 보드만 순서를 변경할 수 있습니다.");
            }
            
            board.updateOrders(update.getNewOrders());
        }
    }
}