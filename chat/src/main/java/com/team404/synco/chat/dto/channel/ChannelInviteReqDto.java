package com.team404.synco.chat.dto.channel;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChannelInviteReqDto {
    private List<Long> memberList;
    private Long memberSeq;
    private Long channelSeq;
    private Long workSpaceSeq;
}
