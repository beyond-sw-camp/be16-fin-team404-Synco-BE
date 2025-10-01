package com.team404.synco.workspace.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class VirtualMeetingChannelCreateReqDto {
    private String virtualMeetingChannelName;
    private Long workSpaceSeq;
}
