package com.team404.synco.virtualmeeting.dto.Feign;

import com.team404.synco.common.constant.Authority;
import com.team404.synco.virtualmeeting.entity.VirtualMeetingChannelMember;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChannelGrantResDto {
    private Long channelSeq;
    private Long workSpaceSeq;
    private String channelName;
    private Long ownerMemberSeq;
    private Long grantMemberSeq;
    private Authority grantAuthority;

    public static ChannelGrantResDto fromEntity(VirtualMeetingChannelMember superMember, VirtualMeetingChannelMember grantMember){
        return ChannelGrantResDto.builder()
                .channelSeq(grantMember.getVirtualMeetingChannel().getVirtualMeetingChannelSeq())
                .workSpaceSeq(grantMember.getVirtualMeetingChannel().getWorkSpaceSeq())
                .channelName(grantMember.getVirtualMeetingChannel().getVirtualMeetingChannelName())
                .ownerMemberSeq(superMember.getMemberSeq())
                .grantMemberSeq(grantMember.getMemberSeq())
                .grantAuthority(grantMember.getAuthority())
                .build();
    }
}
