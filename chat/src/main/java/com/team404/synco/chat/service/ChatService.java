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


    // 채널 생성
    public Long createChannel(ChannelCreateReqDto channelCreateReqDto) {
        ChatChannel chatChannel = chatChannelRepository.save(channelCreateReqDto.toEntity());

        // 채널 생성자 권한 부여 및 저장
        ChatChannelMember creator = ChatChannelMember.builder()
                .memberSeq(channelCreateReqDto.getMemberSeq())
                .authority(Authority.MANAGER)
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

    // 채널 권한 설정
    public void grantToMember(GrantAuthorityReqDto grantAuthorityReqDto, Long memberSeq) throws AccessDeniedException {
        // 기본 채널 멤버 조회
        ChatChannelMember chatChannelMember = chatChannelMemberRepository.findFirstByMemberSeqAndWorkSpaceSeqOrderByChatChannelSeqAsc
                (memberSeq, grantAuthorityReqDto.getWorkSpaceSeq()).orElseThrow(()
                -> new EntityNotFoundException("존재하지 않는 회원입니다."));
        // 현재 사용자의 권한이 super인지 확인
        if (!chatChannelMember.getAuthority().equals(Authority.SUPER)) {
            throw new AccessDeniedException("SUPER 사용자만 권한 변경이 가능합니다.");
        }
        // 대상 멤버 권한 변경
        ChatChannelMember changeAuthorityMember = chatChannelMemberRepository.findFirstByMemberSeqAndWorkSpaceSeqOrderByChatChannelSeqAsc
                (memberSeq, grantAuthorityReqDto.getWorkSpaceSeq()).orElseThrow(()
                -> new EntityNotFoundException("존재하지 않는 회원입니다."));
        String authority = grantAuthorityReqDto.getAuthority();
        switch (authority) {
            case "MANAGER":
                changeAuthorityMember.updateAuthority(Authority.MANAGER);
            case "PARTICIPANT":
                changeAuthorityMember.updateAuthority(Authority.PARTICIPANT);
            default:
                break;
        }
    }

    // 채널 Super 권한 위임
    public void delegateSuperAuthority(DelegateSuperAuthorityReqDto delegateSuperAuthorityReqDto, Long memberSeq)
            throws AccessDeniedException
    {
        // 기본 채널 멤버 조회
        ChatChannelMember chatChannelMember = chatChannelMemberRepository.findFirstByMemberSeqAndWorkSpaceSeqOrderByChatChannelSeqAsc
                (memberSeq, delegateSuperAuthorityReqDto.getWorkSpaceSeq()).orElseThrow(()
                -> new EntityNotFoundException("존재하지 않는 회원입니다."));
        // 현재 사용자의 권한이 super인지 확인
        if (!chatChannelMember.getAuthority().equals(Authority.SUPER)) {
            throw new AccessDeniedException("SUPER 사용자만 권한 변경이 가능합니다.");
        }
        // 대상 멤버 권한 변경
        ChatChannelMember changeAuthorityMember = chatChannelMemberRepository.findFirstByMemberSeqAndWorkSpaceSeqOrderByChatChannelSeqAsc
                (memberSeq, delegateSuperAuthorityReqDto.getWorkSpaceSeq()).orElseThrow(()
                -> new EntityNotFoundException("존재하지 않는 회원입니다."));
        // 위임할 사용자의 권한을 SUPER로 변경
        changeAuthorityMember.updateAuthority(Authority.SUPER);
        // 현재 사용자의 권한을 참여자로 변경
        chatChannelMember.updateAuthority(Authority.PARTICIPANT);
    }

    // 멤버 추가
    public Long addMemberToChannel(ChannelInviteReqDto channelInviteReqDto) {
        ChatChannel chatChannel = chatChannelRepository.findByChatChannelSeqAndWorkSpaceSeq(channelInviteReqDto.getChannelSeq(),
                channelInviteReqDto.getWorkSpaceSeq()).orElseThrow(() ->
                new EntityNotFoundException("등록되지 않은 채널입니다."));
        Optional.ofNullable(channelInviteReqDto.getFriendList()).orElse(Collections.emptyList())
                .stream().filter(Objects::nonNull).map(memberSeq -> ChatChannelMember.builder()
                        .memberSeq(memberSeq)
                        .authority(Authority.PARTICIPANT)
                        .chatChannel(chatChannel)
                        .build())
                .forEach(chatChannelMemberRepository::save);
        return (long) channelInviteReqDto.getFriendList().size();
    }

    // 채널 전체 삭제(WorkSpace 삭제시)
    public void deleteAllChannel(Long workSpaceSeq) {
        chatChannelRepository.deleteAllByWorkSpaceSeq(workSpaceSeq);
    }
}
