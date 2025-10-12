package com.team404.synco.task.dto;

import com.team404.synco.common.constant.Authority;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TaskChannelMemberCreateReqDto {
    private Long memberSeq;
    private Authority authority;
    private Long workSpaceReq;
    private List<Long> friendList;
}
