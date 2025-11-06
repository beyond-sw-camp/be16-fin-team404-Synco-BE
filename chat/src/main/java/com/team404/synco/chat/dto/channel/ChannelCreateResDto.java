package com.team404.synco.chat.dto.channel;

import com.team404.synco.chat.entity.ChatChannel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChannelCreateResDto {
    private Long channelSeq;
    private Long workSpaceSeq;
    private String channelName;

    public static ChannelCreateResDto fromEntity(ChatChannel chatChannel){
        return ChannelCreateResDto.builder()
                .channelSeq(chatChannel.getChatChannelSeq())
                .workSpaceSeq(chatChannel.getWorkSpaceSeq())
                .channelName(chatChannel.getChatChannelName())
                .build();
    }
}
