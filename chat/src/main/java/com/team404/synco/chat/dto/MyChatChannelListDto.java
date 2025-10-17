package com.team404.synco.chat.dto;

import com.team404.synco.chat.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class MyChatChannelListDto {
    private Long chatChannelSeq;
    private String chatChannelName;
    private String isGroupChat;
    private Long unReadCount;
}
