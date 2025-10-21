package com.team404.synco.virtualmeeting.service;

import com.team404.synco.common.constant.Authority;
import com.team404.synco.common.constant.dto.DelegateSuperAuthorityReqDto;
import com.team404.synco.task.common.component.MemberRedisComponent;
import com.team404.synco.virtualmeeting.dto.*;
import com.team404.synco.virtualmeeting.entity.VirtualMeetingChannel;
import com.team404.synco.virtualmeeting.entity.VirtualMeetingChannelMember;
import com.team404.synco.virtualmeeting.repository.VirtualMeetingChannelMemberRepository;
import com.team404.synco.virtualmeeting.repository.VirtualMeetingChannelRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class VirtualMeetingService {
    private final VirtualMeetingChannelRepository virtualMeetingChannelRepository;
    private final VirtualMeetingChannelMemberRepository virtualMeetingChannelMemberRepository;
    private final MemberRedisComponent memberRedisComponent;

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

        // 멤버 채널에 추가
        Optional.ofNullable(channelCreateReqDto.getMemberList()).orElse(Collections.emptyList())
                .stream().filter(Objects::nonNull).map(memberSeq -> VirtualMeetingChannelMember.builder()
                        .memberSeq(memberSeq)
                        .authority(Authority.PARTICIPANT)
                        .virtualMeetingChannel(virtualMeetingChannel)
                        .build())
                .forEach(virtualMeetingChannelMemberRepository::save);
        return virtualMeetingChannel.getVirtualMeetingChannelSeq();
    }


    // 채널 생성
    public ChannelCreateResDto createChannel(ChannelCreateReqDto channelCreateReqDto, Long memberSeq) throws AccessDeniedException {
        // 기본 채널 검증
        VirtualMeetingChannel basicChannel = checkBasicChannel(channelCreateReqDto.getWorkSpaceSeq());
        // 권한 검증
        checkChannelAuthority(basicChannel.getVirtualMeetingChannelSeq(), memberSeq);
        // 새 채널 생성
        VirtualMeetingChannel virtualMeetingChannel = virtualMeetingChannelRepository.save(channelCreateReqDto.toEntity());
        // 초대된 멤버 추가
        List<VirtualMeetingChannelMember> newMembers = basicChannel.getVirtualMeetingChannelmemberList().stream()
                .map(member -> {
                    Authority authority;
                    if (member.getAuthority() == Authority.SUPER) {
                        authority = Authority.SUPER;
                    } else if (member.getMemberSeq() == memberSeq) {
                        authority = Authority.MANAGER;
                    } else {
                        authority = Authority.PARTICIPANT;
                    }

                    return VirtualMeetingChannelMember.builder()
                            .memberSeq(member.getMemberSeq())
                            .authority(authority)
                            .virtualMeetingChannel(virtualMeetingChannel)
                            .build();
                })
                .toList();
        virtualMeetingChannelMemberRepository.saveAll(newMembers);
        return ChannelCreateResDto.fromEntity(virtualMeetingChannel);
    }

    // 채널 이름 수정
    public ChannelEditResDto renameChannel(ChannelEditReqDto channelEditReqDto, Long memberSeq) throws AccessDeniedException {
        // 수정 대상 채널 검증
        VirtualMeetingChannel editChannel = virtualMeetingChannelRepository.findById(channelEditReqDto.getChannelSeq()).orElseThrow(() ->
                new EntityNotFoundException("없는 채널입니다."));
        // 권한 검증
        checkChannelAuthority(editChannel.getVirtualMeetingChannelSeq(), memberSeq);
        // 채널 수정
        editChannel.updateChannelName(channelEditReqDto.getChannelName());
        return ChannelEditResDto.fromEntity(editChannel);
    }

    // 채널 삭제
    public void deleteChannel(Long channelSeq, Long memberSeq) throws AccessDeniedException {
        // 삭제 대상 채널 검증
        VirtualMeetingChannel deleteChannel = virtualMeetingChannelRepository.findById(channelSeq).orElseThrow(() ->
                new EntityNotFoundException("없는 채널입니다."));

        // 기본 채널이 있는지 검증
        VirtualMeetingChannel basicChannel = checkBasicChannel(deleteChannel.getWorkSpaceSeq());

        // 삭제하려는 채널이 기본 채널인지 확인
        if (deleteChannel.equals(basicChannel)) {
            throw new IllegalStateException("기본 채널은 삭제할 수 없습니다.");
        }
        // 권한 검증
        checkChannelAuthority(channelSeq, memberSeq);

        // 채널 삭제
        virtualMeetingChannelRepository.deleteById(deleteChannel.getVirtualMeetingChannelSeq());
    }

    // 채널 권한 설정
    public ChannelGrantResDto grantToMember(GrantAuthorityReqDto grantAuthorityReqDto, Long memberSeq) throws AccessDeniedException {
        // 유효한 프로젝트인지 기본채널 여부를 통해 검증
        VirtualMeetingChannel basicChannel = checkBasicChannel(grantAuthorityReqDto.getWorkSpaceSeq());
        // SUPER 권한 검증
        VirtualMeetingChannelMember superMember = checkAuthorityIsSuper(basicChannel.getVirtualMeetingChannelSeq(), memberSeq);
        // 대상 멤버 조회
        VirtualMeetingChannelMember grantMember = virtualMeetingChannelMemberRepository.findByChannelAndMember
                (basicChannel.getVirtualMeetingChannelSeq(), grantAuthorityReqDto.getGrantMemberSeq()).orElseThrow(()
                -> new EntityNotFoundException("프로젝트의 멤버가 아닙니다."));
        // 권한 변경
        String authority = grantAuthorityReqDto.getAuthority();
        switch (authority) {
            case "MANAGER":
                grantMember.updateAuthority(Authority.MANAGER);
                break;
            case "PARTICIPANT":
                grantMember.updateAuthority(Authority.PARTICIPANT);
                break;
            default:
                break;
        }
        return ChannelGrantResDto.fromEntity(superMember, grantMember);
    }

    // SUPER 권한 위임
    public void delegateSuperAuthority(DelegateSuperAuthorityReqDto delegateSuperAuthorityReqDto, Long memberSeq)
            throws AccessDeniedException {
        // 기본 채널이 있는지 검증
        VirtualMeetingChannel basicChannel = checkBasicChannel(delegateSuperAuthorityReqDto.getWorkSpaceSeq());
        // SUPER 권한 검증
        VirtualMeetingChannelMember superAuthorityMember = checkAuthorityIsSuper(basicChannel.getVirtualMeetingChannelSeq(),
                memberSeq);
        // 대상 멤버 조회
        VirtualMeetingChannelMember changeAuthorityMember = virtualMeetingChannelMemberRepository.
                findByChannelAndMember(basicChannel.getVirtualMeetingChannelSeq(), delegateSuperAuthorityReqDto.getDelegateMemberSeq()).orElseThrow(()
                        -> new EntityNotFoundException("프로젝트의 멤버가 아닙니다.."));
        // 위임할 사용자의 권한을 SUPER로 변경
        changeAuthorityMember.updateAuthority(Authority.SUPER);
        // 현재 사용자의 권한을 참여자로 변경
        superAuthorityMember.updateAuthority(Authority.PARTICIPANT);
    }

    // 모든 채널에 멤버 추가(WorkSpace에 처음 초대되었을때)
    public Long addMemberToChannel(ChannelInviteReqDto channelInviteReqDto, Long memberSeq) throws AccessDeniedException {
        // 기본 채널 조회 (권한 검증용)
        VirtualMeetingChannel basicChannel = checkBasicChannel(channelInviteReqDto.getWorkSpaceSeq());        // 초대한 사람 권한 검증
        checkChannelAuthority(basicChannel.getVirtualMeetingChannelSeq(), memberSeq);
        // 프로젝트 내 모든 채널 조회 (기본 채널 포함)
        List<VirtualMeetingChannel> allChannels = virtualMeetingChannelRepository
                .findByWorkSpaceSeqOrderByVirtualMeetingChannelSeqAsc(channelInviteReqDto.getWorkSpaceSeq());
        // 초대할 멤버들을 모든 채널에 추가
        return Optional.ofNullable(channelInviteReqDto.getMemberList())
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .flatMap(teamMateSeq ->
                        allChannels.stream()
                                // 이미 채널에 존재하는 멤버는 건너뜀
                                .filter(channel -> !virtualMeetingChannelMemberRepository
                                        .existsMember(channel.getVirtualMeetingChannelSeq(), teamMateSeq))
                                .map(channel -> VirtualMeetingChannelMember.builder()
                                        .memberSeq(teamMateSeq)
                                        .authority(Authority.PARTICIPANT)
                                        .virtualMeetingChannel(channel)
                                        .build())
                )
                .map(virtualMeetingChannelMemberRepository::save)
                .count();
    }

    // 채널 리스트
    @Transactional(readOnly = true)
    public List<ChannelInfoResDto> findChatChannelList(Long workSpaceSeq) {
        return virtualMeetingChannelRepository.findByWorkSpaceSeq(workSpaceSeq)
                .stream()
                .map(virtualMeetingChannel -> {
                    List<ChannelMemberResDto> channelMemberResDtoList = virtualMeetingChannel.getVirtualMeetingChannelmemberList()
                            .stream()
                            .map(virtualMeetingChannelMember -> {
                                String memberName = memberRedisComponent.getMemberName(virtualMeetingChannelMember.getMemberSeq())
                                        .replaceAll("^\"|\"$", "");
                                String memberProfileUrl = memberRedisComponent.getMemberProfileUrl(
                                        virtualMeetingChannelMember.getMemberSeq()).replaceAll("^\"|\"$", "");
                                return ChannelMemberResDto.of(virtualMeetingChannelMember, memberName, memberProfileUrl);
                            })
                            .toList();

                    return ChannelInfoResDto.of(virtualMeetingChannel, channelMemberResDtoList);
                })
                .toList();
    }

    // 채널 전체 삭제(WorkSpace 삭제시)
    public void deleteAllChannel(Long workSpaceSeq) {
        virtualMeetingChannelRepository.deleteAllByWorkSpaceSeq(workSpaceSeq);
    }

    // 프로젝트 탈퇴
    public void deleteMemberFromWorkSpace(Long workSpaceSeq, Long memberSeq){
        // 기본 채널 조회 (권한 검증용)
        VirtualMeetingChannel basicChannel = checkBasicChannel(workSpaceSeq);

        // 멤버가 채널에 있는지 확인 / 채널에 있는 모든 멤버 행 다 가져오기
        virtualMeetingChannelMemberRepository.findByChannelAndMember(basicChannel.getVirtualMeetingChannelSeq(),
                memberSeq).orElseThrow(() -> new EntityNotFoundException("프로젝트의 멤버가 아닙니다."));

        virtualMeetingChannelMemberRepository.deleteByChannelAndMember(basicChannel.getVirtualMeetingChannelSeq(), memberSeq);
    }

    // 기본 채널 검증
    private VirtualMeetingChannel checkBasicChannel(Long workSpaceSeq) {
        return virtualMeetingChannelRepository.findFirstByWorkSpaceSeqOrderByVirtualMeetingChannelSeqAsc(workSpaceSeq).orElseThrow(() ->
                new EntityNotFoundException("기본 채널이 존재하지 않습니다. 유효하지 않은 WorkSpace입니다."));
    }

    // 채널 권한 검증
    private void checkChannelAuthority(Long channelSeq, Long memberSeq) throws AccessDeniedException {
        VirtualMeetingChannelMember virtualMeetingChannelMember = virtualMeetingChannelMemberRepository.findByChannelAndMember(channelSeq,
                memberSeq).orElseThrow(() -> new EntityNotFoundException("프로젝트의 멤버가 아닙니다."));

        if (!virtualMeetingChannelMember.getAuthority().equals(Authority.SUPER) && !virtualMeetingChannelMember
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
