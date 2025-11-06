package com.team404.synco.workspace.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChannelInviteReqDto {
    private Long channelSeq;
    private Long workSpaceSeq;
    private List<Long> memberList;
}
