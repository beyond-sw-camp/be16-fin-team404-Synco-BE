package com.team404.synco.task.dto;

import com.team404.synco.common.constant.Authority;
import com.team404.synco.task.entity.ScheduleManagementChannelMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class TaskChannelMemberCreateReqDto {
    private Long memberSeq;
    private Authority authority;
    private Long workSpaceReq;

    public ScheduleManagementChannelMember toEntity(){
        return ScheduleManagementChannelMember.builder()
                .memberSeq(this.memberSeq)
                .authority(this.authority)
                .workSpaceSeq(this.workSpaceReq)
                .build();
    }
}
