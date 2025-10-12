package com.team404.synco.virtualmeeting.dto;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChannelInviteReqDto {
    private List<Long> friendList;
    private Long memberSeq;
    private Long channelSeq;
    private Long workSpaceSeq;
}