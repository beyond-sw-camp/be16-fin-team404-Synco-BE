package com.team404.synco.virtualmeeting.dto;

import com.team404.synco.virtualmeeting.entity.VirtualMeetingChannel;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ChannelCreateReqDto {
    private String channelName;
    private Long workSpaceSeq;
    private List<Long> friendList;
    private Long memberSeq;

    public VirtualMeetingChannel toEntity(){
        return VirtualMeetingChannel.builder()
                .virtualMeetingChannelName(this.channelName)
                .workSpaceSeq(this.workSpaceSeq)
                .build();
    }
}
