package com.team404.synco.chat.service;

import com.team404.synco.chat.dto.ChatChannelCreateReqDto;
import com.team404.synco.chat.entity.ChatChannel;
import com.team404.synco.chat.entity.ChatChannelMember;
import com.team404.synco.chat.repository.ChatChannelRepository;
import com.team404.synco.common.constant.Authority;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ChatService {
    private final ChatChannelRepository chatChannelRepository;

    public ChatService(ChatChannelRepository chatChannelRepository) {
        this.chatChannelRepository = chatChannelRepository;
    }

    // 채널 생성
    // ToDO : 우선은 기본 채널 생성만 작업했습니다. 추후 채널 추가 가능하도록 코드 수정 예정입니다.
    public Long createChannel(ChatChannelCreateReqDto chatChannelCreateReqDto){
        ChatChannel chatChannel = ChatChannel.builder()
                .chatChannelName(chatChannelCreateReqDto.getChatChannelName())
                .workSpaceSeq(chatChannelCreateReqDto.getWorkSpaceSeq())
                .build();

        ChatChannelMember chatChannelMember = ChatChannelMember.builder()
                .memberSeq(1L)
                .authority(Authority.SUPER)
                .chatChannel(chatChannel)
                .build();

        chatChannel.getChatChannelMemberList().add(chatChannelMember);
        return chatChannelRepository.save(chatChannel).getChatChannelSeq();
    }
}
