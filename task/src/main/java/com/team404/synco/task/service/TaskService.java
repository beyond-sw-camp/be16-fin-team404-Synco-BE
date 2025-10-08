package com.team404.synco.task.service;

import com.team404.synco.common.constant.Authority;
import com.team404.synco.task.dto.TaskChannelMemberCreateReqDto;
import com.team404.synco.task.entity.ScheduleManagementChannelMember;
import com.team404.synco.task.repository.ScheduleManagementChannelMemberRepository;
import com.team404.synco.virtualmeeting.dto.ChannelInviteReqDto;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@Slf4j
public class TaskService {
    private final ScheduleManagementChannelMemberRepository scheduleManagementChannelMemberRepository;

    public TaskService(ScheduleManagementChannelMemberRepository scheduleManagementChannelMemberRepository) {
        this.scheduleManagementChannelMemberRepository = scheduleManagementChannelMemberRepository;
    }

    // 팀 task 생성
    public void createTaskChannel(TaskChannelMemberCreateReqDto taskChannelMemberCreateReqDto){
        // 채널 생성자 권한 부여 및 저장
        ScheduleManagementChannelMember creator = ScheduleManagementChannelMember.builder()
                .memberSeq(taskChannelMemberCreateReqDto.getMemberSeq())
                .authority(Authority.SUPER)
                .workSpaceSeq(taskChannelMemberCreateReqDto.getWorkSpaceReq())
                .build();
        scheduleManagementChannelMemberRepository.save(creator);

        List<Long> memberList = taskChannelMemberCreateReqDto.getMemberList();
        if (memberList != null && !memberList.isEmpty()) {
            for (Long memberSeq : memberList) {
                ScheduleManagementChannelMember scheduleManagementChannelMember = ScheduleManagementChannelMember.builder()
                        .memberSeq(memberSeq)
                        .authority(Authority.PARTICIPANT)
                        .workSpaceSeq(taskChannelMemberCreateReqDto.getWorkSpaceReq())
                        .build();
                scheduleManagementChannelMemberRepository.save(scheduleManagementChannelMember);
            }
        }
    }

    // 멤버 추가
    public Long addMemberToChannel(ChannelInviteReqDto channelInviteReqDto){
        List<Long> memberList = channelInviteReqDto.getMemberList();
        for(Long memberSeq : memberList){
            ScheduleManagementChannelMember scheduleManagementChannelMember = ScheduleManagementChannelMember.builder()
                    .memberSeq(memberSeq)
                    .authority(Authority.PARTICIPANT)
                    .build();
            scheduleManagementChannelMemberRepository.save(scheduleManagementChannelMember);
        }
        return (long) channelInviteReqDto.getMemberList().size();
    }
}
