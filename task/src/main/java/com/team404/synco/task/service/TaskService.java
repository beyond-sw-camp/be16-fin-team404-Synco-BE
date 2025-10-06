package com.team404.synco.task.service;

import com.team404.synco.task.dto.TaskChannelMemberCreateReqDto;
import com.team404.synco.task.entity.ScheduleManagementChannelMember;
import com.team404.synco.task.repository.ScheduleManagementChannelMemberRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Transactional
@Slf4j
public class TaskService {
    private final ScheduleManagementChannelMemberRepository scheduleManagementChannelMemberRepository;

    public TaskService(ScheduleManagementChannelMemberRepository scheduleManagementChannelMemberRepository) {
        this.scheduleManagementChannelMemberRepository = scheduleManagementChannelMemberRepository;
    }

    // 팀 task 생성
    public Long createTaskChannel(TaskChannelMemberCreateReqDto taskChannelMemberCreateReqDto){
        return scheduleManagementChannelMemberRepository.save(taskChannelMemberCreateReqDto.toEntity()).getScheduleManagementChannelMemberSeq();
    }
}
