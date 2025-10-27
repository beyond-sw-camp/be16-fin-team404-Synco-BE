package com.team404.synco.task.service;

import com.team404.synco.common.constant.Authority;
import com.team404.synco.task.constant.TaskStatus;
import com.team404.synco.task.dto.request.PersonalTaskCreateReqDto;
import com.team404.synco.task.dto.request.PersonalTaskUpdateReqDto;
import com.team404.synco.task.dto.request.TaskStatusUpdateReqDto;
import com.team404.synco.task.dto.response.PersonalTaskResDto;
import com.team404.synco.task.dto.response.TasksResDto;
import com.team404.synco.task.entity.ScheduleManagementChannelMember;
import com.team404.synco.task.entity.Task;
import com.team404.synco.task.repository.ScheduleManagementChannelMemberRepository;
import com.team404.synco.task.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonalScheduleManagementService {

    private final TaskRepository taskRepository;
    private final ScheduleManagementChannelMemberRepository scheduleManagementChannelMemberRepository;

    // 개인 스케줄 Task 생성
    @Transactional
    public Long createPersonalTask(Long memberSeq, Long workSpaceSeq, PersonalTaskCreateReqDto createReqDto) {
        ScheduleManagementChannelMember member = scheduleManagementChannelMemberRepository
                .findByMemberSeqAndWorkSpaceSeq(memberSeq, workSpaceSeq)
                .orElseThrow(() -> new EntityNotFoundException("일정관리 채널 멤버를 찾을 수 없습니다."));

        return taskRepository.save(createReqDto.toEntity(member)).getTaskSeq();
    }

    // 개인 스케줄 Task 조회 (상태별로 그룹화)
    @Transactional(readOnly = true)
    public List<TasksResDto> getPersonalTasks(Long memberSeq, Long workSpaceSeq) {
        ScheduleManagementChannelMember member = scheduleManagementChannelMemberRepository
                .findByMemberSeqAndWorkSpaceSeq(memberSeq, workSpaceSeq)
                .orElseThrow(() -> new EntityNotFoundException("일정관리 채널 멤버를 찾을 수 없습니다."));

        List<Task> tasks = taskRepository.findByPicMemberSeq(member.getScheduleManagementChannelMemberSeq());
        
        // Task를 상태별로 그룹화
        Map<TaskStatus, List<Task>> groupedTasks = tasks.stream()
                .collect(Collectors.groupingBy(Task::getTaskStatus));
        
        return groupedTasks.entrySet().stream()
                .map(entry -> TasksResDto.fromEntity(entry.getKey(), entry.getValue()))
                .toList();
    }

    // 개인 스케줄 Task 상세 조회
    @Transactional(readOnly = true)
    public PersonalTaskResDto getPersonalTask(Long taskSeq, Long memberSeq) {
        Task task = taskRepository.findById(taskSeq)
                .orElseThrow(() -> new EntityNotFoundException("Task를 찾을 수 없습니다."));

        // 요청자가 해당 Task의 워크스페이스에 참여하는지 검증
        scheduleManagementChannelMemberRepository
                .findByMemberSeqAndWorkSpaceSeq(memberSeq, task.getPicMemberSeq().getWorkSpaceSeq())
                .orElseThrow(() -> new EntityNotFoundException("해당 워크스페이스에 참여하지 않은 사용자입니다."));

        return PersonalTaskResDto.fromEntity(task);
    }

    // 개인 스케줄 Task 수정
    @Transactional
    public void updatePersonalTask(Long taskSeq, Long memberSeq, PersonalTaskUpdateReqDto updateReqDto) {
        Task task = taskRepository.findById(taskSeq)
                .orElseThrow(() -> new EntityNotFoundException("Task를 찾을 수 없습니다."));

        // 요청자가 해당 Task의 워크스페이스에 참여하는지 검증
        scheduleManagementChannelMemberRepository
                .findByMemberSeqAndWorkSpaceSeq(memberSeq, task.getPicMemberSeq().getWorkSpaceSeq())
                .orElseThrow(() -> new EntityNotFoundException("해당 워크스페이스에 참여하지 않은 사용자입니다."));

        task.updatePersonalTask(updateReqDto);
    }

    // 개인 스케줄 Task 상태만 변경 (칸반보드 드래그 앤 드롭용)
    @Transactional
    public void updatePersonalTaskStatus(Long taskSeq, Long memberSeq, TaskStatusUpdateReqDto statusUpdateReqDto) {
        Task task = taskRepository.findById(taskSeq)
                .orElseThrow(() -> new EntityNotFoundException("Task를 찾을 수 없습니다."));

        // 요청자가 해당 Task의 워크스페이스에 참여하는지 검증
        scheduleManagementChannelMemberRepository
                .findByMemberSeqAndWorkSpaceSeq(memberSeq, task.getPicMemberSeq().getWorkSpaceSeq())
                .orElseThrow(() -> new EntityNotFoundException("해당 워크스페이스에 참여하지 않은 사용자입니다."));

        task.updateTaskStatus(statusUpdateReqDto.getTaskStatus());
    }

    // 개인 스케줄 Task 삭제
    @Transactional
    public void deletePersonalTask(Long taskSeq, Long memberSeq) {
        Task task = taskRepository.findById(taskSeq)
                .orElseThrow(() -> new EntityNotFoundException("Task를 찾을 수 없습니다."));

        // 요청자가 해당 Task의 워크스페이스에 참여하는지 검증
        scheduleManagementChannelMemberRepository
                .findByMemberSeqAndWorkSpaceSeq(memberSeq, task.getPicMemberSeq().getWorkSpaceSeq())
                .orElseThrow(() -> new EntityNotFoundException("해당 워크스페이스에 참여하지 않은 사용자입니다."));

        taskRepository.delete(task);
    }
}
