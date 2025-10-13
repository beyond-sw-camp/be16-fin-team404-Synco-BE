package com.team404.synco.workspace.dto;

import lombok.*;

import java.util.List;

@Getter
@Builder
public class ChannelInviteReqDto {
    private Long ChannelSeq;
    private Long workSpaceSeq;
    private List<Long> friendList;
}
