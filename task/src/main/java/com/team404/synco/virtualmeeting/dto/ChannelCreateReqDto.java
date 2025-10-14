package com.team404.synco.virtualmeeting.dto;

import com.team404.synco.virtualmeeting.entity.VirtualMeetingChannel;
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
    private String channelName;
    private Long workSpaceSeq;
    private Long memberSeq;
    private List<Long> friendList;

    public VirtualMeetingChannel toEntity(){
        return VirtualMeetingChannel.builder()
                .virtualMeetingChannelName(this.channelName)
                .workSpaceSeq(this.workSpaceSeq)
                .build();
    }
}
