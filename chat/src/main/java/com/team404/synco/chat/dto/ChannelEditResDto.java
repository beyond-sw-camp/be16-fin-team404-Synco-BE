package com.team404.synco.chat.dto;

import com.team404.synco.chat.entity.ChatChannel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChannelEditResDto {
    private Long channelSeq;
    private String channelName;

    public static ChannelEditResDto fromEntity(ChatChannel chatChannel){
        return ChannelEditResDto.builder()
                .channelSeq(chatChannel.getChatChannelSeq())
                .channelName(chatChannel.getChatChannelName())
                .build();
    }
}
