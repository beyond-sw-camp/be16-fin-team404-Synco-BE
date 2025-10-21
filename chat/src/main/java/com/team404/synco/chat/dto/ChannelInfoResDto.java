package com.team404.synco.chat.dto;

import com.team404.synco.chat.entity.ChatChannel;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChannelInfoResDto {
    private Long channelSeq;
    private Long workSpaceSeq;
    private String channelName;
    private List<ChannelMemberResDto> channelMemberList;

    public static ChannelInfoResDto of(ChatChannel chatChannel, List<ChannelMemberResDto> channelMemberList){
        return ChannelInfoResDto.builder()
                .channelSeq(chatChannel.getChatChannelSeq())
                .workSpaceSeq(chatChannel.getWorkSpaceSeq())
                .channelName(chatChannel.getChatChannelName())
                .channelMemberList(channelMemberList)
                .build();
    }
}
