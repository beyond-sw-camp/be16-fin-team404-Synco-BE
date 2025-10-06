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
    private String ChannelName;
    private Long workSpaceSeq;
    private List<Long> memberList;
    private Long memberSeq;

    public ChatChannel toEntity(){
        return ChatChannel.builder()
                .chatChannelName("일반")
                .workSpaceSeq(this.workSpaceSeq)
                .build();
    }
}
