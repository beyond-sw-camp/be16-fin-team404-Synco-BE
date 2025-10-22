package com.team404.synco.virtualmeeting.dto;

import com.team404.synco.common.constant.Authority;
import com.team404.synco.virtualmeeting.entity.VirtualMeetingChannelMember;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChannelMemberResDto {
    private Long memberSeq;
    private Authority authority;
    private String memberName;
    private String memberProfileUrl;

    public static ChannelMemberResDto of(VirtualMeetingChannelMember chatChannelMember, String memberName, String memberProfileUrl){
        return ChannelMemberResDto.builder()
                .memberSeq(chatChannelMember.getMemberSeq())
                .authority(chatChannelMember.getAuthority())
                .memberName(memberName)
                .memberProfileUrl(memberProfileUrl)
                .build();
    }
}
