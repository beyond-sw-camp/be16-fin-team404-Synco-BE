package com.team404.synco.task.dto.response;


import com.team404.synco.task.entity.ScheduleManagementChannelMember;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WorkspaceMemberDto {
    private Long scheduleManagementChannelMemberSeq;
    private Long memberSeq;
    private String memberName;
    private String profileImageUrl;

    public static WorkspaceMemberDto fromEntity(ScheduleManagementChannelMember member, String memberName, String profileImageUrl) {
        return WorkspaceMemberDto.builder()
                .scheduleManagementChannelMemberSeq(member.getScheduleManagementChannelMemberSeq())
                .memberSeq(member.getMemberSeq())
                .memberName(memberName)
                .profileImageUrl(profileImageUrl)
                .build();
    }
}
