package com.team404.synco.task.dto;

import com.team404.synco.common.constant.Authority;
import com.team404.synco.task.entity.ScheduleManagementChannelMember;
import lombok.Builder;
import lombok.Getter;
 
@Getter
@Builder
public class ChannelMemberResDto {
    private Long memberSeq;
    private Authority authority;
    private String memberName;
    private String memberProfileUrl;

    public static ChannelMemberResDto of(ScheduleManagementChannelMember scheduleManagementChannelMember, String memberName, String memberProfileUrl){
        return ChannelMemberResDto.builder()
                .memberSeq(scheduleManagementChannelMember.getMemberSeq())
                .authority(scheduleManagementChannelMember.getAuthority())
                .memberName(memberName)
                .memberProfileUrl(memberProfileUrl)
                .build();
    }
}
