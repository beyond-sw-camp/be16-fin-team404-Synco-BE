package com.team404.synco.task.service;

import com.team404.synco.task.dto.TaskCreateReqDto;
import com.team404.synco.task.entity.ScheduleManagementChannelMember;
import com.team404.synco.task.repository.ScheduleManagementChannelMemberRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class TaskService {
    private final ScheduleManagementChannelMemberRepository scheduleManagementChannelMemberRepository;

    public TaskService(ScheduleManagementChannelMemberRepository scheduleManagementChannelMemberRepository) {
        this.scheduleManagementChannelMemberRepository = scheduleManagementChannelMemberRepository;
    }

    // 팀 task 생성
    public Long createTask(TaskCreateReqDto taskCreateReqDto){
        ScheduleManagementChannelMember scheduleManagementChannelMember = ScheduleManagementChannelMember.builder()
                .memberSeq(taskCreateReqDto.getMemberSeq())
                .authority(taskCreateReqDto.getAuthority())
                .build();
        return scheduleManagementChannelMemberRepository.save(scheduleManagementChannelMember).getScheduleManagementChannelMemberSeq();
    }
}
