package com.team404.synco.chat.service;

import com.team404.synco.chat.dto.*;
import com.team404.synco.chat.entity.ChatChannel;
import com.team404.synco.chat.entity.ChatChannelMember;
import com.team404.synco.chat.repository.ChatChannelMemberRepository;
import com.team404.synco.chat.repository.ChatChannelRepository;
import com.team404.synco.common.constant.Authority;
import com.team404.synco.common.service.MemberRedisComponent;
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
public class ChatService {
    private final ChatChannelRepository chatChannelRepository;
    private final ChatChannelMemberRepository chatChannelMemberRepository;
    private final MemberRedisComponent memberRedisComponent;

    // 기본 채널 생성
    public Long createBasicChannel(ChannelCreateReqDto channelCreateReqDto) {
        ChatChannel chatChannel = chatChannelRepository.save(channelCreateReqDto.toEntity());

        // 채널 생성자 권한 부여 및 저장
        ChatChannelMember creator = ChatChannelMember.builder()
                .memberSeq(channelCreateReqDto.getMemberSeq())
                .authority(Authority.SUPER)
                .chatChannel(chatChannel)
                .build();
        chatChannelMemberRepository.save(creator);
        Optional.ofNullable(channelCreateReqDto.getMemberList()).orElse(Collections.emptyList())
                .stream().filter(Objects::nonNull).map(memberSeq -> ChatChannelMember.builder()
                        .memberSeq(memberSeq)
                        .authority(Authority.PARTICIPANT)
                        .chatChannel(chatChannel)
                        .build())
                .forEach(chatChannelMemberRepository::save);
        return chatChannel.getChatChannelSeq();
    }

    // 채널 생성(1:1 채팅 채널)
    // ToDo : 채팅 담당자는 1:1 채팅 시작할때 이 로직으로 채팅 채널 생성하시면 됩니다.
    public Long createIndividualChatChannel(ChannelCreateReqDto channelCreateReqDto) {
        ChatChannel chatChannel = chatChannelRepository.save(channelCreateReqDto.toEntity());

        // 채팅 대상 list에 추가
        Optional.ofNullable(channelCreateReqDto.getMemberList()).orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .map(friendSeq -> ChatChannelMember.builder()
                        .memberSeq(friendSeq)
                        .chatChannel(chatChannel)
                        .authority(Authority.SUPER)
                        .build())
                .forEach(chatChannelMemberRepository::save);
        return chatChannel.getChatChannelSeq();
    }

    // 채널 생성(팀)
    public ChannelCreateResDto createChannel(ChannelCreateReqDto channelCreateReqDto, Long memberSeq) throws AccessDeniedException {
        // 기본 채널 검증
        ChatChannel basicChannel = checkBasicChannel(channelCreateReqDto.getWorkSpaceSeq());
        // 권한 검증
        checkChannelAuthority(basicChannel.getChatChannelSeq(), memberSeq);
        // 새 채널 생성
        ChatChannel chatChannel = chatChannelRepository.save(channelCreateReqDto.toEntity());
        // 기존 채널 멤버를 새 채널에 추가
        List<ChatChannelMember> newMembers = basicChannel.getChatChannelmemberList().stream()
                .map(member -> {
                    Authority authority;
                    if (member.getAuthority() == Authority.SUPER) {
                        authority = Authority.SUPER;
                    } else if (member.getMemberSeq() == memberSeq) {
                        authority = Authority.MANAGER;
                    } else {
                        authority = Authority.PARTICIPANT;
                    }

                    return ChatChannelMember.builder()
                            .memberSeq(member.getMemberSeq())
                            .authority(authority)
                            .chatChannel(chatChannel)
                            .build();
                })
                .toList();
        chatChannelMemberRepository.saveAll(newMembers);
        return ChannelCreateResDto.fromEntity(chatChannel);
    }

    // 채널 이름 수정
    public ChannelEditResDto renameChannel(ChannelEditReqDto channelEditReqDto, Long memberSeq) throws AccessDeniedException {
        // 수정 대상 채널 검증
        ChatChannel editChannel = chatChannelRepository.findById(channelEditReqDto.getChannelSeq()).orElseThrow(() ->
                new EntityNotFoundException("없는 채널입니다."));
        // 권한 검증
        checkChannelAuthority(editChannel.getChatChannelSeq(), memberSeq);
        // 채널 수정
        editChannel.updateChannelName(channelEditReqDto.getChannelName());
        return ChannelEditResDto.fromEntity(editChannel);
    }

    // 채널 삭제
    public void deleteChannel(Long channelSeq, Long memberSeq) throws AccessDeniedException {
        // 삭제 대상 채널 검증
        ChatChannel deleteChannel = chatChannelRepository.findById(channelSeq).orElseThrow(() ->
                new EntityNotFoundException("없는 채널입니다."));
        // 기본 채널이 있는지 검증
        ChatChannel basicChannel = checkBasicChannel(deleteChannel.getWorkSpaceSeq());
        // 삭제하려는 채널이 기본 채널인지 확인
        if (deleteChannel.equals(basicChannel)) {
            throw new IllegalStateException("기본 채널은 삭제할 수 없습니다.");
        }
        // 권한 검증
        checkChannelAuthority(channelSeq, memberSeq);
        // 채널 삭제
        chatChannelRepository.deleteById(deleteChannel.getChatChannelSeq());
    }

    // 채널 권한 설정
    public ChannelGrantResDto grantToMember(GrantAuthorityReqDto grantAuthorityReqDto, Long memberSeq) throws AccessDeniedException {
        // 유효한 워크스페이스인지 기본채널 여부를 통해 검증
        ChatChannel basicChannel = checkBasicChannel(grantAuthorityReqDto.getWorkSpaceSeq());
        // SUPER 권한 검증
        ChatChannelMember superMember = checkAuthorityIsSuper(basicChannel.getChatChannelSeq(), memberSeq);
        // 대상 멤버 조회
        ChatChannelMember grantMember = chatChannelMemberRepository.findByChannelAndMember
                (basicChannel.getChatChannelSeq(), grantAuthorityReqDto.getGrantMemberSeq()).orElseThrow(()
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

    // 채널 SUPER 권한 위임
    public void delegateSuperAuthority(DelegateSuperAuthorityReqDto delegateSuperAuthorityReqDto, Long memberSeq)
            throws AccessDeniedException {
        // 기본 채널이 있는지 검증
        ChatChannel basicChannel = checkBasicChannel(delegateSuperAuthorityReqDto.getWorkSpaceSeq());
        // SUPER 권한 검증
        ChatChannelMember superAuthorityMember = checkAuthorityIsSuper(basicChannel.getChatChannelSeq(), memberSeq);
        // 대상 멤버 조회
        ChatChannelMember changeAuthorityMember = chatChannelMemberRepository.findByChannelAndMember
                (basicChannel.getChatChannelSeq(), delegateSuperAuthorityReqDto.getDelegateMemberSeq()).orElseThrow(()
                -> new EntityNotFoundException("프로젝트의 멤버가 아닙니다.."));
        // 위임할 사용자의 권한을 SUPER로 변경
        changeAuthorityMember.updateAuthority(Authority.SUPER);
        // 현재 사용자의 권한을 참여자로 변경
        superAuthorityMember.updateAuthority(Authority.PARTICIPANT);
    }

    // 모든 채널에 멤버 추가(WorkSpace에 처음 초대되었을때)
    public Long addMemberToChannel(ChannelInviteReqDto channelInviteReqDto, Long memberSeq) throws AccessDeniedException {
        // 기본 채널 조회 (권한 검증용)
        ChatChannel basicChannel = checkBasicChannel(channelInviteReqDto.getWorkSpaceSeq());
        // 초대한 사람 권한 검증
        checkChannelAuthority(basicChannel.getChatChannelSeq(), memberSeq);
        // 워크스페이스 내 모든 채널 조회 (기본 채널 포함)
        List<ChatChannel> allChannels = chatChannelRepository
                .findByWorkSpaceSeqOrderByChatChannelSeqAsc(channelInviteReqDto.getWorkSpaceSeq());
        // 초대할 멤버들을 모든 채널에 추가
        return Optional.ofNullable(channelInviteReqDto.getMemberList())
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .flatMap(teamMateSeq ->
                        allChannels.stream()
                                // 이미 채널에 존재하는 멤버는 건너뜀
                                .filter(channel -> !chatChannelMemberRepository
                                        .existsMember(channel.getChatChannelSeq(), teamMateSeq))
                                .map(channel -> ChatChannelMember.builder()
                                        .memberSeq(teamMateSeq)
                                        .authority(Authority.PARTICIPANT)
                                        .chatChannel(channel)
                                        .build())
                )
                .map(chatChannelMemberRepository::save)
                .count();
    }

    // 채널 리스트
    @Transactional(readOnly = true)
    public List<ChannelInfoResDto> findChatChannelList(Long workSpaceSeq) {
        return chatChannelRepository.findByWorkSpaceSeq(workSpaceSeq)
                .stream()
                .map(chatChannel -> {
                    List<ChannelMemberResDto> channelMemberResDtoList = chatChannel.getChatChannelmemberList()
                            .stream()
                            .map(chatChannelMember -> {
                                String memberName = memberRedisComponent.getMemberName(chatChannelMember.getMemberSeq())
                                        .replaceAll("^\"|\"$", "");;
                                String memberProfileUrl = memberRedisComponent.getMemberProfileUrl(
                                        chatChannelMember.getMemberSeq()).replaceAll("^\"|\"$", "");    ;
                                return ChannelMemberResDto.of(chatChannelMember, memberName, memberProfileUrl);
                            })
                            .toList();

                    return ChannelInfoResDto.of(chatChannel, channelMemberResDtoList);
                })
                .toList();
    }

    // 채널 전체 삭제(Team WorkSpace 삭제시)
    public void deleteAllChannel(Long workSpaceSeq) {
        chatChannelRepository.deleteAllByWorkSpaceSeq(workSpaceSeq);
    }

    // 워크스페이스 탈퇴
    public void deleteMemberFromWorkSpace(Long workSpaceSeq, Long memberSeq){
        // 기본 채널 조회 (권한 검증용)
        ChatChannel basicChannel = checkBasicChannel(workSpaceSeq);
        log.info("권한 검증 성공");
        // 멤버가 채널에 있는지 확인
        chatChannelMemberRepository.findByChannelAndMember(basicChannel.getChatChannelSeq(),
                memberSeq).orElseThrow(() -> new EntityNotFoundException("프로젝트의 멤버가 아닙니다."));
        log.info("멤버 프로젝트 존재 여부 검증 성공");
        chatChannelMemberRepository.deleteByChannelAndMember(basicChannel.getChatChannelSeq(), memberSeq);
    }

    // 기본 채널 검증
    private ChatChannel checkBasicChannel(Long workSpaceSeq) {
        return chatChannelRepository.findFirstByWorkSpaceSeqOrderByChatChannelSeqAsc(workSpaceSeq).orElseThrow(() ->
                new EntityNotFoundException("기본 채널이 존재하지 않습니다. 유효하지 않은 WorkSpace입니다."));
    }

    // 채널 권한 검증
    private void checkChannelAuthority(Long channelSeq, Long memberSeq) throws AccessDeniedException {
        ChatChannelMember chatChannelMember = chatChannelMemberRepository.findByChannelAndMember(channelSeq,
                memberSeq).orElseThrow(() -> new EntityNotFoundException("프로젝트의 멤버가 아닙니다."));

        if (!chatChannelMember.getAuthority().equals(Authority.SUPER) &&
                !chatChannelMember.getAuthority().equals(Authority.MANAGER)) {
            throw new AccessDeniedException("초대 권한이 없습니다.");
        }
    }

    // SUPER 권한 검증
    private ChatChannelMember checkAuthorityIsSuper(Long channelSeq, Long memberSeq) throws AccessDeniedException {
        // 기본 채널 멤버 조회
        ChatChannelMember chatChannelMember = chatChannelMemberRepository.
                findByChannelAndMember(channelSeq, memberSeq).orElseThrow(()
                        -> new EntityNotFoundException("프로젝트의 멤버가 아닙니다."));
        // 현재 사용자의 권한이 super인지 확인
        if (!chatChannelMember.getAuthority().equals(Authority.SUPER)) {
            throw new AccessDeniedException("SUPER 사용자만 권한 변경이 가능합니다.");
        }
        return chatChannelMember;
    }
}
