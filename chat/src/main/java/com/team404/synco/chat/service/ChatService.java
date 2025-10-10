package com.team404.synco.chat.service;

import com.team404.synco.chat.dto.ChannelCreateReqDto;
import com.team404.synco.chat.dto.ChannelInviteReqDto;
import com.team404.synco.chat.entity.ChatChannel;
import com.team404.synco.chat.entity.ChatChannelMember;
import com.team404.synco.chat.repository.ChatChannelMemberRepository;
import com.team404.synco.chat.repository.ChatChannelRepository;
import com.team404.synco.common.constant.Authority;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

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

    // 채널 생성
    // ToDO : 우선은 기본 채널 생성만 작업했습니다. 추후 채널 추가 가능하도록 코드 수정 예정입니다.
    public Long createChannel(ChannelCreateReqDto channelCreateReqDto) {
        ChatChannel chatChannel = chatChannelRepository.save(channelCreateReqDto.toEntity());

        // 채널 생성자 권한 부여 및 저장
        ChatChannelMember creator = ChatChannelMember.builder()
                .memberSeq(channelCreateReqDto.getMemberSeq())
                .authority(Authority.SUPER)
                .chatChannel(chatChannel)
                .build();
        chatChannelMemberRepository.save(creator);

        List<Long> friendList = channelCreateReqDto.getFriendList();
        if (friendList != null && !friendList.isEmpty()) {
            for (Long memberSeq : friendList) {
                ChatChannelMember chatChannelMember = ChatChannelMember.builder()
                        .memberSeq(memberSeq)
                        .authority(Authority.PARTICIPANT)
                        .chatChannel(chatChannel)
                        .build();
                chatChannelMemberRepository.save(chatChannelMember);
            }
        }

        return chatChannel.getChatChannelSeq();
    }

    // 멤버 추가
    public Long addMemberToChannel(ChannelInviteReqDto channelInviteReqDto) {
        ChatChannel chatChannel = chatChannelRepository.findByChatChannelSeqAndWorkSpaceSeq(channelInviteReqDto.getChannelSeq(),
                channelInviteReqDto.getWorkSpaceSeq()).orElseThrow(() ->
                new EntityNotFoundException("등록되지 않은 채널입니다."));
        List<Long> friendList = channelInviteReqDto.getFriendList();
        for (Long memberSeq : friendList) {
            ChatChannelMember chatChannelMember = ChatChannelMember.builder()
                    .memberSeq(memberSeq)
                    .authority(Authority.PARTICIPANT)
                    .chatChannel(chatChannel)
                    .build();
            chatChannelMemberRepository.save(chatChannelMember);
        }
        return (long) channelInviteReqDto.getFriendList().size();
    }

    // 채널 전체 삭제(WorkSpace 삭제시)
    public void deleteAllChannel(Long workSpaceSeq){
        chatChannelRepository.deleteAllByWorkSpaceSeq(workSpaceSeq);
    }
}
