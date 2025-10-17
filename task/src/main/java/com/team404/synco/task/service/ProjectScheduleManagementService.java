package com.team404.synco.task.service;

import com.team404.synco.common.constant.Authority;
import com.team404.synco.task.common.component.MemberRedisComponent;
import com.team404.synco.task.common.domain.MemberInfo;
import com.team404.synco.task.constant.TaskStatus;
import com.team404.synco.task.dto.request.BoardCreateRequestDto;
import com.team404.synco.task.dto.request.TaskCreateRequestDto;
import com.team404.synco.task.dto.response.BoardResponseDto;
import com.team404.synco.task.dto.response.TasksResponseDto;
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

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamScheduleManagementService {
    private final BoardRepository boardRepository;
    private final TaskRepository taskRepository;
    private final MemberRedisComponent memberRedisComponent;
    private final ScheduleManagementChannelMemberRepository scheduleManagementChannelMemberRepository;

    @Transactional(readOnly = true)
    public List<TasksResponseDto> getAllTasksResponseDtoList(final long workSpaceSeq) {
        final Map<TaskStatus, List<Task>> groupedTasks = taskRepository.findAllByWorkSpaceSeqOrderByCreatedAsc(workSpaceSeq).stream()
                .collect(Collectors.groupingBy(Task::getTaskStatus));
        return groupedTasks.entrySet().stream()
                .map(entry -> TasksResponseDto.builder()
                        .taskStatusDescription(entry.getKey().getDisplayName())
                        .taskResponseDtoList(entry.getValue().stream()
                                .map(task -> TasksResponseDto.TaskResponseDto.builder()
                                        .taskSeq(task.getTaskSeq())
                                        .taskTitle(task.getTaskTitle())
                                        .taskStatus(task.getTaskStatus())
                                        .startDate(task.getStartDate())
                                        .endDate(task.getEndDate())
                                        .picMemberSeq(task.getPicMemberSeq().getMemberSeq())
////                                        .picMemberName(task.getPicMember().getName())
////                                        .picMemberProfileImageUrl(task.getPicMember().getProfileImageUrl())
                                        .build()).toList())
                        .build()).toList();
    }

    @Transactional
    public long createTeamTask(final long memberSeq, final TaskCreateRequestDto taskCreateRequestDto) {
        final ScheduleManagementChannelMember picMember = scheduleManagementChannelMemberRepository
                .findById(taskCreateRequestDto.getPicMemberSeq()).orElseThrow(() -> new EntityNotFoundException("일정관리 채널 업무 담당자 회원을 찾을 수 없습니다."));
        final ScheduleManagementChannelMember createMember = scheduleManagementChannelMemberRepository.findByMemberSeqAndWorkSpaceSeq(memberSeq, picMember.getWorkSpaceSeq())
                .orElseThrow(() -> new EntityNotFoundException("일정관리 채널 생성자 회원을 찾을 수 없습니다."));
        if (createMember.getAuthority() == Authority.PARTICIPANT) {
            throw new ForbiddenException("권한이 유효하지 않습니다.");
        }
        Optional<Board> board = Optional.empty();
        if(taskCreateRequestDto.getBoardSeq() != null) {
            board = boardRepository.findById(taskCreateRequestDto.getBoardSeq());
            if (board.isEmpty()) {
                throw new EntityNotFoundException("일정관리 채널 보드를 찾을 수 없습니다.");
            }
        }
        return taskCreateRequestDto.toEntity(picMember, board).getTaskSeq();
    }

    @Transactional(readOnly = true)
    public List<BoardResponseDto> fetchBoardsWithTasksByChannelMember(final long memberSeq, final long workSpaceSeq) {
        List<Board> boardList = boardRepository.findAllByMemberAndWorkSpace(memberSeq, workSpaceSeq);
        return boardList.stream().map(board -> {
            Map<Long, MemberInfo> memberInfoMap = board.getTaskList().stream().collect(Collectors.toMap(Task::getTaskSeq,
                    task -> MemberInfo.builder().memberSeq(memberSeq).memberName(memberRedisComponent.getMemberName(memberSeq))
                            .memberProfileImageUrl(memberRedisComponent.getMemberProfileUrl(memberSeq)).build()));
            return BoardResponseDto.fromEntity(board, memberInfoMap);
        }).toList();
    }

    @Transactional
    public long createBoard(final BoardCreateRequestDto boardCreateRequestDto) {
        final ScheduleManagementChannelMember scheduleManagementChannelMember = scheduleManagementChannelMemberRepository
                .findById(boardCreateRequestDto.getScheduleManagementChannelMemberSeq())
                .orElseThrow(() -> new EntityNotFoundException("일정관리 채널 회원을 찾을수 없습니다."));
        return boardRepository.save(boardCreateRequestDto.toEntity(scheduleManagementChannelMember)).getBoardSeq();
    }
}
