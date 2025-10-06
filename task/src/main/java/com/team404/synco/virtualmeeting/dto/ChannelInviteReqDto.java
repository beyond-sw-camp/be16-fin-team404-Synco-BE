package com.team404.synco.virtualmeeting.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChannelInviteReqDto {
    private List<Long> memberList;
    private Long memberSeq;
    private Long chatChannelSeq;
}
