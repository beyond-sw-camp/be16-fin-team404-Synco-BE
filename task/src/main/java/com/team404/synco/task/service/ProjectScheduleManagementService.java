package com.team404.synco.task.service;

import com.team404.synco.common.constant.Authority;
import com.team404.synco.task.common.component.MemberRedisComponent;
import com.team404.synco.task.common.domain.MemberInfo;
import com.team404.synco.task.constant.TaskStatus;
import com.team404.synco.task.dto.request.BoardCreateReqDto;
import com.team404.synco.task.dto.request.TaskCreateReqDto;
import com.team404.synco.task.dto.response.BoardResDto;
import com.team404.synco.task.dto.response.TasksResDto;
import com.team404.synco.task.dto.response.WorkspaceMemberDto;
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
    public List<TasksResDto> getAllTasksResponseDtoList(final long workSpaceSeq) {
    final Map<TaskStatus, List<Task>> groupedTasks = taskRepository.findAllByWorkSpaceSeqOrderByCreatedAsc(workSpaceSeq).stream()
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

    // 보드 생성
    @Transactional
    public long createProjectBoard(final BoardCreateReqDto boardCreateReqDto) {
        final ScheduleManagementChannelMember scheduleManagementChannelMember = scheduleManagementChannelMemberRepository
                .findById(boardCreateReqDto.getScheduleManagementChannelMemberSeq())
                .orElseThrow(() -> new EntityNotFoundException("일정관리 채널 회원을 찾을수 없습니다."));

        long maxOrder = boardRepository.findMaxOrderByWorkSpaceSeq(scheduleManagementChannelMember.getWorkSpaceSeq())
                .orElse(0L);

        Board board = boardCreateReqDto.toEntity(scheduleManagementChannelMember, maxOrder + 1);

        return boardRepository.save(board).getBoardSeq();
    }

    // 워크스페이스 멤버 목록 조회
    @Transactional(readOnly = true)
    public List<WorkspaceMemberDto> getWorkspaceMemberList(final long workSpaceSeq) {
        List<ScheduleManagementChannelMember> memberList = scheduleManagementChannelMemberRepository
                .findAllByWorkSpaceSeq(workSpaceSeq).orElseThrow(() -> new EntityNotFoundException("조회되는 워크스페이스 목록이 없습니다."));;

        return memberList.stream()
                .map(member -> WorkspaceMemberDto.fromEntity(
                        member,
                        memberRedisComponent.getMemberName(member.getMemberSeq()),
                        memberRedisComponent.getMemberProfileUrl(member.getMemberSeq())
                ))
                .toList();
    }
}
