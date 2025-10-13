package com.team404.synco.chat.service;

import com.team404.synco.chat.dto.ChannelCreateReqDto;
import com.team404.synco.chat.dto.ChannelInviteReqDto;
import com.team404.synco.chat.dto.DelegateSuperAuthorityReqDto;
import com.team404.synco.chat.dto.GrantAuthorityReqDto;
import com.team404.synco.chat.entity.ChatChannel;
import com.team404.synco.chat.entity.ChatChannelMember;
import com.team404.synco.chat.repository.ChatChannelMemberRepository;
import com.team404.synco.chat.repository.ChatChannelRepository;
import com.team404.synco.common.constant.Authority;
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
public class ChatService {
    private final ChatChannelRepository chatChannelRepository;
    private final ChatChannelMemberRepository chatChannelMemberRepository;

    public ChatService(ChatChannelRepository chatChannelRepository, ChatChannelMemberRepository chatChannelMemberRepository) {
        this.chatChannelRepository = chatChannelRepository;
        this.chatChannelMemberRepository = chatChannelMemberRepository;
    }

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

        Optional.ofNullable(channelCreateReqDto.getFriendList()).orElse(Collections.emptyList())
                .stream().filter(Objects::nonNull).map(memberSeq -> ChatChannelMember.builder()
                        .memberSeq(memberSeq)
                        .authority(Authority.PARTICIPANT)
                        .chatChannel(chatChannel)
                        .build())
                .forEach(chatChannelMemberRepository::save);
        return chatChannel.getChatChannelSeq();
    }

    // 채널 생성(1:1 채팅 채널)
    public Long createIndividualChatChannel(ChannelCreateReqDto channelCreateReqDto){
        ChatChannel chatChannel = chatChannelRepository.save(channelCreateReqDto.toEntity());

        // 채팅 대상 list에 추가
        Optional.ofNullable(channelCreateReqDto.getFriendList()).orElse(Collections.emptyList())
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
    public Long createChannel(ChannelCreateReqDto channelCreateReqDto, Long memberSeq) throws AccessDeniedException {
        // 기본 채널이 있는지 검증
        ChatChannel basicChannel = chatChannelRepository.
                findFirstByWorkSpaceSeqOrderByChatChannelSeqAsc(channelCreateReqDto.getWorkSpaceSeq()).orElseThrow(() ->
                        new EntityNotFoundException("기본 채널이 존재하지 않습니다. 유효하지 않은 WorkSpace입니다."));
        // 권한 검증
        checkInviteAndCreateChannelAuthority(basicChannel.getChatChannelSeq(), memberSeq);

        ChatChannel chatChannel = chatChannelRepository.save(channelCreateReqDto.toEntity());

        // 채널 생성자 권한 부여 및 저장
        ChatChannelMember creator = ChatChannelMember.builder()
                .memberSeq(memberSeq)
                .authority(Authority.MANAGER)
                .chatChannel(chatChannel)
                .build();
        chatChannelMemberRepository.save(creator);

        Optional.ofNullable(channelCreateReqDto.getFriendList()).orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .map(friendSeq -> ChatChannelMember.builder()
                        .memberSeq(friendSeq)
                        .authority(Authority.PARTICIPANT)
                        .chatChannel(chatChannel)
                        .build())
                .forEach(chatChannelMemberRepository::save);
        return chatChannel.getChatChannelSeq();
    }

    // 채널 권한 설정
    public void grantToMember(GrantAuthorityReqDto grantAuthorityReqDto, Long memberSeq) throws AccessDeniedException {
        // 기본 채널이 있는지 검증
        ChatChannel basicChannel = chatChannelRepository.
                findFirstByWorkSpaceSeqOrderByChatChannelSeqAsc(grantAuthorityReqDto.getWorkSpaceSeq()).orElseThrow(() ->
                        new EntityNotFoundException("기본 채널이 존재하지 않습니다. 유효하지 않은 WorkSpace입니다."));
        // SUPER 권한 검증
        checkAuthorityIsSuper(basicChannel.getChatChannelSeq(), memberSeq);
        // 대상 멤버 조회
        ChatChannelMember changeAuthorityMember = chatChannelMemberRepository.findByChannelAndMember
                (grantAuthorityReqDto.getChannelSeq(), grantAuthorityReqDto.getGrantMemberSeq()).orElseThrow(()
                -> new EntityNotFoundException("프로젝트의 멤버가 아닙니다."));
        // 권한 변경
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

    // 채널 SUPER 권한 위임
    public void delegateSuperAuthority(DelegateSuperAuthorityReqDto delegateSuperAuthorityReqDto, Long memberSeq)
            throws AccessDeniedException {
        log.info("chatFeign 호출 시작");
        // 기본 채널이 있는지 검증
        ChatChannel basicChannel = chatChannelRepository.
                findFirstByWorkSpaceSeqOrderByChatChannelSeqAsc(delegateSuperAuthorityReqDto.getWorkSpaceSeq()).orElseThrow(() ->
                        new EntityNotFoundException("기본 채널이 존재하지 않습니다. 유효하지 않은 WorkSpace입니다."));
        // SUPER 권한 검증
        ChatChannelMember superAuthorityMember = checkAuthorityIsSuper(delegateSuperAuthorityReqDto.getWorkSpaceSeq(), memberSeq);
        // 대상 멤버 조회
        ChatChannelMember changeAuthorityMember = chatChannelMemberRepository.findByChannelAndMember
                (basicChannel.getChatChannelSeq(), delegateSuperAuthorityReqDto.getDelegateMemberSeq()).orElseThrow(()
                -> new EntityNotFoundException("프로젝트의 멤버가 아닙니다.."));
        // 위임할 사용자의 권한을 SUPER로 변경
        changeAuthorityMember.updateAuthority(Authority.SUPER);
        // 현재 사용자의 권한을 참여자로 변경
        superAuthorityMember.updateAuthority(Authority.PARTICIPANT);
        log.info("chatFeign 호출 완료");
    }

    // 멤버 추가
    public Long addMemberToChannel(ChannelInviteReqDto channelInviteReqDto, Long memberSeq) throws AccessDeniedException {
        ChatChannel checkIsFirstChannel = chatChannelRepository.
                findFirstByWorkSpaceSeqOrderByChatChannelSeqAsc(channelInviteReqDto.getWorkSpaceSeq()).orElseThrow(() ->
                        new EntityNotFoundException("기본 채널이 존재하지 않습니다."));
        // 권한 검증
        checkInviteAndCreateChannelAuthority(checkIsFirstChannel.getChatChannelSeq(), memberSeq);

        ChatChannel chatChannel;
        Long channelSeq = channelInviteReqDto.getChannelSeq();
        // 채널 번호가 있으면 해당 채널로 설정
        if (channelSeq != null && channelSeq > 0) {
            chatChannel = chatChannelRepository.findByChatChannelSeqAndWorkSpaceSeq(channelInviteReqDto.getChannelSeq(),
                    channelInviteReqDto.getWorkSpaceSeq()).orElseThrow(() ->
                    new EntityNotFoundException("등록되지 않은 채널입니다."));
            channelInviteReqDto.setChannelSeq(chatChannel.getChatChannelSeq());
            // 없으면 기본 채널로 설정
        } else {
            chatChannel = checkIsFirstChannel;
        }

        return Optional.ofNullable(channelInviteReqDto.getFriendList())
                .orElse(Collections.emptyList()).stream()
                .filter(Objects::nonNull)
                .peek(teamMateSeq -> {
                    // 🔥 중복 멤버 예외 처리
                    if (chatChannelMemberRepository.existsMember(chatChannel.getChatChannelSeq(),
                            teamMateSeq)) {
                        throw new IllegalStateException("이미 채널에 존재하는 멤버입니다: " + teamMateSeq);
                    }
                })
                .map(teamMateSeq -> ChatChannelMember.builder()
                        .memberSeq(teamMateSeq)
                        .authority(Authority.PARTICIPANT)
                        .chatChannel(chatChannel)
                        .build())
                .map(chatChannelMemberRepository::save)
                .count();
    }

    // 채널 전체 삭제(WorkSpace 삭제시)
    public void deleteAllChannel(Long workSpaceSeq) {
        chatChannelRepository.deleteAllByWorkSpaceSeq(workSpaceSeq);
    }


    // 초대, 채널 생성 권한 검증
    private void checkInviteAndCreateChannelAuthority(Long channelSeq, Long memberSeq) throws AccessDeniedException {
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
