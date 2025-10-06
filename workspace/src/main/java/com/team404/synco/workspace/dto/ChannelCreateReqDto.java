package com.team404.synco.workspace.dto;

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
    private Long memberSeq;
    private List<Long> memberList;
}
