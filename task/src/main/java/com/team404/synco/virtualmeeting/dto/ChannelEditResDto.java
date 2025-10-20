package com.team404.synco.virtualmeeting.dto;

import com.team404.synco.virtualmeeting.entity.VirtualMeetingChannel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChannelEditResDto {
    private Long channelSeq;
    private String channelName;

    public static ChannelEditResDto fromEntity(VirtualMeetingChannel virtualMeetingChannel){
        return ChannelEditResDto.builder()
                .channelSeq(virtualMeetingChannel.getVirtualMeetingChannelSeq())
                .channelName(virtualMeetingChannel.getVirtualMeetingChannelName())
                .build();
    }
}
