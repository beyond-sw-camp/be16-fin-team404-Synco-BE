package com.team404.synco.task.service;

import com.team404.synco.common.constant.Authority;
import com.team404.synco.common.constant.dto.DelegateSuperAuthorityReqDto;
import com.team404.synco.task.dto.TaskChannelMemberCreateReqDto;
import com.team404.synco.task.entity.ScheduleManagementChannelMember;
import com.team404.synco.task.repository.ScheduleManagementChannelMemberRepository;
import com.team404.synco.virtualmeeting.dto.ChannelInviteReqDto;
import com.team404.synco.virtualmeeting.dto.GrantAuthorityReqDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class TaskService {
    private final ScheduleManagementChannelMemberRepository scheduleManagementChannelMemberRepository;

    public TaskService(ScheduleManagementChannelMemberRepository scheduleManagementChannelMemberRepository) {
        this.scheduleManagementChannelMemberRepository = scheduleManagementChannelMemberRepository;
    }

    // 팀 task 생성
    public void createTaskChannel(TaskChannelMemberCreateReqDto taskChannelMemberCreateReqDto) {
        // 채널 생성자 권한 부여 및 저장
        ScheduleManagementChannelMember creator = ScheduleManagementChannelMember.builder()
                .memberSeq(taskChannelMemberCreateReqDto.getMemberSeq())
                .authority(Authority.SUPER)
                .workSpaceSeq(taskChannelMemberCreateReqDto.getWorkSpaceReq())
                .build();
        scheduleManagementChannelMemberRepository.save(creator);

        Optional.ofNullable(taskChannelMemberCreateReqDto.getMemberList())
                .orElse(Collections.emptyList()) // null이면 빈 리스트로 대체
                .stream()
                .filter(Objects::nonNull)
                .map(memberSeq -> ScheduleManagementChannelMember.builder()
                        .memberSeq(memberSeq)
                        .authority(Authority.PARTICIPANT)
                        .workSpaceSeq(taskChannelMemberCreateReqDto.getWorkSpaceReq())
                        .build())
                .forEach(scheduleManagementChannelMemberRepository::save);
    }

    // 채널 권한 설정
    public void grantToMember(GrantAuthorityReqDto grantAuthorityReqDto, Long memberSeq) throws AccessDeniedException {
        checkInviteAndCreateChannelAuthority(grantAuthorityReqDto.getWorkSpaceSeq(), memberSeq);
        // 대상 멤버 권한 변경
        ScheduleManagementChannelMember changeAuthorityMember = scheduleManagementChannelMemberRepository.
                findByWorkSpaceSeqAndMemberSeq(grantAuthorityReqDto.getWorkSpaceSeq(), grantAuthorityReqDto.getGrantMemberSeq()).orElseThrow(()
                        -> new EntityNotFoundException("프로젝트의 멤버가 아닙니다.."));
        String authority = grantAuthorityReqDto.getAuthority();
        switch (authority) {
            case "MANAGER":
                changeAuthorityMember.updateAuthority(Authority.MANAGER);
                break;
            case "PARTICIPANT":
                changeAuthorityMember.updateAuthority(Authority.PARTICIPANT);
                break;
            default:
                break;
        }
    }

    // 초대, 채널 생성 권한 검증
    private void checkInviteAndCreateChannelAuthority(Long workSpaceSeq, Long memberSeq) throws AccessDeniedException {
        ScheduleManagementChannelMember scheduleManagementChannelMember = scheduleManagementChannelMemberRepository.findByWorkSpaceSeqAndMemberSeq(workSpaceSeq,
                memberSeq).orElseThrow(() -> new EntityNotFoundException("프로젝트의 멤버가 아닙니다."));

        if (!scheduleManagementChannelMember.getAuthority().equals(Authority.SUPER) &&
                !scheduleManagementChannelMember.getAuthority().equals(Authority.MANAGER)) {
            throw new AccessDeniedException("초대 권한이 없습니다.");
        }
    }

    // SUPER 권한 위임
    public void delegateSuperAuthority(DelegateSuperAuthorityReqDto delegateSuperAuthorityReqDto, Long memberSeq)
            throws AccessDeniedException {
        // 현재 사용자의 권한이 super인지 확인
        ScheduleManagementChannelMember scheduleManagementChannelMember =
                checkAuthorityIsSuper(delegateSuperAuthorityReqDto.getWorkSpaceSeq(), memberSeq);
        // 대상 멤버 권한 변경
        ScheduleManagementChannelMember changeAuthorityMember = scheduleManagementChannelMemberRepository.
                findByWorkSpaceSeqAndMemberSeq(delegateSuperAuthorityReqDto.getWorkSpaceSeq(), delegateSuperAuthorityReqDto.getDelegateMemberSeq()).orElseThrow(()
                        -> new EntityNotFoundException("프로젝트의 멤버가 아닙니다.."));
        // 위임할 사용자의 권한을 SUPER로 변경
        changeAuthorityMember.updateAuthority(Authority.SUPER);
        // 현재 사용자의 권한을 참여자로 변경
        scheduleManagementChannelMember.updateAuthority(Authority.PARTICIPANT);
    }

    // 멤버 추가
    public Long addMemberToChannel(ChannelInviteReqDto channelInviteReqDto) {
        List<Long> memberList = Optional.ofNullable(channelInviteReqDto.getMemberList())
                .orElse(Collections.emptyList());

        return memberList.stream().filter(Objects::nonNull).map(memberSeq -> ScheduleManagementChannelMember.builder()
                        .memberSeq(memberSeq)
                        .authority(Authority.PARTICIPANT)
                        .workSpaceSeq(channelInviteReqDto.getWorkSpaceSeq())
                        .build())
                .map(scheduleManagementChannelMemberRepository::save) // save된 객체 반환
                .count();
    }

    // 팀 Task 전체 삭제(WorkSpace 삭제시)
    public void deleteAllTask(Long workSpaceSeq) {
        scheduleManagementChannelMemberRepository.deleteByWorkSpaceSeq(workSpaceSeq);
    }

    // SUPER 권한 검증
    private ScheduleManagementChannelMember checkAuthorityIsSuper(Long workSpaceSeq, Long memberSeq) throws AccessDeniedException {
        // 조회
        ScheduleManagementChannelMember scheduleManagementChannelMember = scheduleManagementChannelMemberRepository.
                findByWorkSpaceSeqAndMemberSeq(workSpaceSeq, memberSeq).orElseThrow(()
                        -> new EntityNotFoundException("프로젝트의 멤버가 아닙니다."));
        // 현재 사용자의 권한이 super인지 확인
        if (!scheduleManagementChannelMember.getAuthority().equals(Authority.SUPER)) {
            throw new AccessDeniedException("SUPER 사용자만 권한 변경이 가능합니다.");
        }
        return scheduleManagementChannelMember;
    }
}
