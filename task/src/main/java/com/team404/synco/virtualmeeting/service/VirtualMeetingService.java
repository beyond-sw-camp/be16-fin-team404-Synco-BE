package com.team404.synco.virtualmeeting.service;

import com.team404.synco.common.constant.Authority;
import com.team404.synco.common.constant.dto.DelegateSuperAuthorityReqDto;
import com.team404.synco.virtualmeeting.dto.ChannelCreateReqDto;
import com.team404.synco.virtualmeeting.dto.ChannelInviteReqDto;
import com.team404.synco.virtualmeeting.dto.GrantAuthorityReqDto;
import com.team404.synco.virtualmeeting.entity.VirtualMeetingChannel;
import com.team404.synco.virtualmeeting.entity.VirtualMeetingChannelMember;
import com.team404.synco.virtualmeeting.repository.VirtualMeetingChannelMemberRepository;
import com.team404.synco.virtualmeeting.repository.VirtualMeetingChannelRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class VirtualMeetingService {
    private final VirtualMeetingChannelRepository virtualMeetingChannelRepository;
    private final VirtualMeetingChannelMemberRepository virtualMeetingChannelMemberRepository;

    public VirtualMeetingService(VirtualMeetingChannelRepository virtualMeetingChannelRepository,
                                 VirtualMeetingChannelMemberRepository virtualMeetingChannelMemberRepository) {
        this.virtualMeetingChannelRepository = virtualMeetingChannelRepository;
        this.virtualMeetingChannelMemberRepository = virtualMeetingChannelMemberRepository;
    }

    // 기본 채널 생성
    public Long createBasicChannel(ChannelCreateReqDto channelCreateReqDto) {
        VirtualMeetingChannel virtualMeetingChannel = virtualMeetingChannelRepository.save(channelCreateReqDto.toEntity());

        // 채널 생성자 권한 부여 및 저장
        VirtualMeetingChannelMember creator = VirtualMeetingChannelMember.builder()
                .memberSeq(channelCreateReqDto.getMemberSeq())
                .authority(Authority.SUPER)
                .virtualMeetingChannel(virtualMeetingChannel)
                .build();
        virtualMeetingChannelMemberRepository.save(creator);

        Optional.ofNullable(channelCreateReqDto.getFriendList()).orElse(Collections.emptyList())
                .stream().filter(Objects::nonNull).map(memberSeq -> VirtualMeetingChannelMember.builder()
                        .memberSeq(memberSeq)
                        .authority(Authority.PARTICIPANT)
                        .virtualMeetingChannel(virtualMeetingChannel)
                        .build())
                .forEach(virtualMeetingChannelMemberRepository::save);
        return virtualMeetingChannel.getVirtualMeetingChannelSeq();
    }


    // 채널 생성
    public Long createChannel(ChannelCreateReqDto channelCreateReqDto, Long memberSeq) throws AccessDeniedException {
        // 기본 채널이 있는지 검증
        VirtualMeetingChannel basicChannel = virtualMeetingChannelRepository.
                findFirstByWorkSpaceSeqOrderByVirtualMeetingChannelSeqAsc(channelCreateReqDto.getWorkSpaceSeq()).orElseThrow(() ->
                        new EntityNotFoundException("기본 채널이 존재하지 않습니다. 유효하지 않은 WorkSpace입니다."));

        // 권한 검증
        checkInviteAndCreateChannelAuthority(basicChannel.getVirtualMeetingChannelSeq(), memberSeq);
        VirtualMeetingChannel virtualMeetingChannel = virtualMeetingChannelRepository.save(channelCreateReqDto.toEntity());

        VirtualMeetingChannelMember virtualMeetingChannelMember = VirtualMeetingChannelMember.builder()
                .memberSeq(channelCreateReqDto.getMemberSeq())
                .authority(Authority.SUPER)
                .virtualMeetingChannel(virtualMeetingChannel)
                .build();
        virtualMeetingChannelMemberRepository.save(virtualMeetingChannelMember);

        // 초대된 멤버 추가
        Optional.ofNullable(channelCreateReqDto.getFriendList()).orElse(Collections.emptyList())
                .stream().filter(Objects::nonNull).map(friendSeq -> VirtualMeetingChannelMember.builder()
                        .memberSeq(friendSeq)
                        .authority(Authority.PARTICIPANT)
                        .virtualMeetingChannel(virtualMeetingChannel)
                        .build())
                .forEach(virtualMeetingChannelMemberRepository::save);
        return virtualMeetingChannel.getVirtualMeetingChannelSeq();
    }

    // 채널 권한 설정
    public void grantToMember(GrantAuthorityReqDto grantAuthorityReqDto, Long memberSeq) throws AccessDeniedException {
        // SUPER 권한 검증
        checkAuthorityIsSuper(grantAuthorityReqDto.getChannelSeq(), memberSeq);
        // 대상 멤버 조회
        VirtualMeetingChannelMember changeAuthorityMember = virtualMeetingChannelMemberRepository.findByChannelAndMember
                (grantAuthorityReqDto.getChannelSeq(), grantAuthorityReqDto.getGrantMemberSeq()).orElseThrow(()
                -> new EntityNotFoundException("프로젝트의 멤버가 아닙니다."));
        // 권한 변경
        String authority = String.valueOf(changeAuthorityMember.getAuthority());
        switch (authority) {
            case "MANAGER":
                changeAuthorityMember.updateAuthority(Authority.MANAGER);
            case "PARTICIPANT":
                changeAuthorityMember.updateAuthority(Authority.PARTICIPANT);
            default:
                break;
        }
    }

    // SUPER 권한 위임
    public void delegateSuperAuthority(DelegateSuperAuthorityReqDto delegateSuperAuthorityReqDto, Long memberSeq)
            throws AccessDeniedException {
        // SUPER 권한 검증
        VirtualMeetingChannelMember superAuthorityMember = checkAuthorityIsSuper(memberSeq, delegateSuperAuthorityReqDto.getWorkSpaceSeq());
        // 대상 멤버 조회
        VirtualMeetingChannelMember changeAuthorityMember = virtualMeetingChannelMemberRepository.
                findByChannelAndMember(memberSeq, delegateSuperAuthorityReqDto.getWorkSpaceSeq()).orElseThrow(()
                        -> new EntityNotFoundException("프로젝트의 멤버가 아닙니다.."));
        // 위임할 사용자의 권한을 SUPER로 변경
        changeAuthorityMember.updateAuthority(Authority.SUPER);
        // 현재 사용자의 권한을 참여자로 변경
        superAuthorityMember.updateAuthority(Authority.PARTICIPANT);
    }

    // 멤버 추가
    public Long addMemberToChannel(ChannelInviteReqDto channelInviteReqDto, Long memberSeq) throws AccessDeniedException {
        VirtualMeetingChannel checkIsFirstChannel = virtualMeetingChannelRepository
                .findFirstByWorkSpaceSeqOrderByVirtualMeetingChannelSeqAsc(channelInviteReqDto.getWorkSpaceSeq())
                .orElseThrow(() -> new EntityNotFoundException("기본 채널이 존재하지 않습니다. 유효하지 않은 WorkSpace입니다."));

        // 권한 검증
        checkInviteAndCreateChannelAuthority(checkIsFirstChannel.getVirtualMeetingChannelSeq(), memberSeq);

        VirtualMeetingChannel virtualMeetingChannel;
        Long channelSeq = channelInviteReqDto.getChannelSeq();

        // 채널 번호가 있으면 해당 채널로 설정
        if (channelSeq != null && channelSeq > 0) {
            virtualMeetingChannel = virtualMeetingChannelRepository
                    .findByVirtualMeetingChannelSeqAndWorkSpaceSeq(
                            channelInviteReqDto.getChannelSeq(),
                            channelInviteReqDto.getWorkSpaceSeq()
                    )
                    .orElseThrow(() -> new EntityNotFoundException("등록되지 않은 채널입니다."));
        } else {
            // 없으면 기본 채널로 설정
            virtualMeetingChannel = checkIsFirstChannel;
        }

        return Optional.ofNullable(channelInviteReqDto.getFriendList())
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .peek(teamMateSeq -> {
                    // 🔥 중복 멤버 예외 처리
                    if (virtualMeetingChannelMemberRepository.existsMember(virtualMeetingChannel.getVirtualMeetingChannelSeq(),
                            teamMateSeq)) {
                        throw new IllegalStateException("이미 채널에 존재하는 멤버입니다: " + teamMateSeq);
                    }
                })
                .map(teamMateSeq -> VirtualMeetingChannelMember.builder()
                        .memberSeq(teamMateSeq)
                        .authority(Authority.PARTICIPANT)
                        .virtualMeetingChannel(virtualMeetingChannel)
                        .build())
                .map(virtualMeetingChannelMemberRepository::save)
                .count();
    }

    // 채널 전체 삭제(WorkSpace 삭제시)
    public void deleteAllChannel(Long workSpaceSeq) {
        virtualMeetingChannelRepository.deleteAllByWorkSpaceSeq(workSpaceSeq);
    }

    // 초대, 채널 생성 권한 검증
    private void checkInviteAndCreateChannelAuthority(Long channelSeq, Long memberSeq) throws AccessDeniedException {
        VirtualMeetingChannelMember virtualMeetingChannelMember = virtualMeetingChannelMemberRepository.findByChannelAndMember(channelSeq,
                memberSeq).orElseThrow(() -> new EntityNotFoundException("프로젝트의 멤버가 아닙니다."));

        if (!virtualMeetingChannelMember.getAuthority().equals(Authority.SUPER) && virtualMeetingChannelMember
                .getAuthority().equals(Authority.MANAGER)) {
            throw new AccessDeniedException("초대 권한이 없습니다.");
        }
    }

    // SUPER 권한 검증
    private VirtualMeetingChannelMember checkAuthorityIsSuper(Long channelSeq, Long memberSeq) throws AccessDeniedException {
        // 기본 채널 멤버 조회
        VirtualMeetingChannelMember virtualMeetingChannelMember = virtualMeetingChannelMemberRepository.
                findByChannelAndMember(channelSeq, memberSeq).orElseThrow(()
                        -> new EntityNotFoundException("프로젝트의 멤버가 아닙니다."));
        // 현재 사용자의 권한이 super인지 확인
        if (!virtualMeetingChannelMember.getAuthority().equals(Authority.SUPER)) {
            throw new AccessDeniedException("SUPER 사용자만 권한 변경이 가능합니다.");
        }
        return virtualMeetingChannelMember;
    }
}
