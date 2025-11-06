package com.team404.synco.chat.dto.channel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChannelEditReqDto {
    private Long channelSeq;
    private String channelName;
}
