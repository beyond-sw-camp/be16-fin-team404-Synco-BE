package com.team404.synco.virtualmeeting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class VirtualMeetingChannelCreateReqDto {
    private String virtualMeetingChannelName;
    private Long workSpaceSeq;
}
