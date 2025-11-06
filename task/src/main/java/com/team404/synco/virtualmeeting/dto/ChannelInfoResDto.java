package com.team404.synco.virtualmeeting.dto;

import com.team404.synco.virtualmeeting.entity.VirtualMeetingChannel;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChannelInfoResDto {
    private Long channelSeq;
    private Long workSpaceSeq;
    private String channelName;
    private List<ChannelMemberResDto> channelMemberList;

    public static ChannelInfoResDto of(VirtualMeetingChannel virtualMeetingChannel, List<ChannelMemberResDto> channelMemberList){
        return ChannelInfoResDto.builder()
                .channelSeq(virtualMeetingChannel.getVirtualMeetingChannelSeq())
                .workSpaceSeq(virtualMeetingChannel.getWorkSpaceSeq())
                .channelName(virtualMeetingChannel.getVirtualMeetingChannelName())
                .channelMemberList(channelMemberList)
                .build();
    }
}
