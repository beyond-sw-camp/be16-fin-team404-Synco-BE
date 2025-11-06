package com.team404.synco.virtualmeeting.dto.Feign;

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