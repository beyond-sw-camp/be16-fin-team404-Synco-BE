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
public class TaskChannelMemberCreateReqDto {
    private Long memberSeq;
    private Long workSpaceReq;
    private List<Long> friendList;
}