package com.team404.synco.workspace.dto;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ChannelCreateReqDto {
    private String channelName;
    private Long workSpaceSeq;
    private Long memberSeq;
    private List<Long> friendList;
}
