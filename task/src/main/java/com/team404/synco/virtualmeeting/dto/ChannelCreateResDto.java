package com.team404.synco.virtualmeeting.dto;

import com.team404.synco.virtualmeeting.entity.VirtualMeetingChannel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChannelCreateResDto {
    private Long channelSeq;
    private Long workSpaceSeq;
    private String channelName;

    public static ChannelCreateResDto fromEntity(VirtualMeetingChannel virtualMeetingChannel){
        return ChannelCreateResDto.builder()
                .channelSeq(virtualMeetingChannel.getVirtualMeetingChannelSeq())
                .workSpaceSeq(virtualMeetingChannel.getWorkSpaceSeq())
                .channelName(virtualMeetingChannel.getVirtualMeetingChannelName())
                .build();
    }
}
