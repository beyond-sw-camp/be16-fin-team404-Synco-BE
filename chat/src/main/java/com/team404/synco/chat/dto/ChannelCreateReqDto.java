package com.team404.synco.chat.dto;

import com.team404.synco.chat.entity.ChatChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ChannelCreateReqDto {
    private String channelName;
    private Long workSpaceSeq;
    private Long memberSeq;
    private List<Long> memberList;

    public ChatChannel toEntity(){
        return ChatChannel.builder()
                .chatChannelName(this.channelName)
                .workSpaceSeq(this.workSpaceSeq)
                .build();
    }
}