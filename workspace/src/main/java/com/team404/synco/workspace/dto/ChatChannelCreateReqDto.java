package com.team404.synco.workspace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ChatChannelCreateReqDto {
    private String chatChannelName;
    private Long workSpaceSeq;
}
