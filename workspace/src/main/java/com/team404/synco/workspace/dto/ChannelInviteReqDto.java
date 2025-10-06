package com.team404.synco.workspace.dto;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ChannelInviteReqDto {
    private Long ChannelSeq;
    private List<Long> memberList;
}
