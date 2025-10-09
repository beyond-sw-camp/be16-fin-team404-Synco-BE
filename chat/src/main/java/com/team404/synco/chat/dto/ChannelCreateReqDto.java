package com.team404.synco.chat.dto;

import com.team404.synco.chat.entity.ChatChannel;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ChannelCreateReqDto {
    private String channelName;
    private Long workSpaceSeq;
    private List<Long> friendList;
    private Long memberSeq;

    public ChatChannel toEntity(){
        return ChatChannel.builder()
                .chatChannelName(this.channelName)
                .workSpaceSeq(this.workSpaceSeq)
                .build();
    }
}
