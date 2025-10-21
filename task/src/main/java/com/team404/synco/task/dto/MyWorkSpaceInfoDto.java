package com.team404.synco.task.dto;

import com.team404.synco.task.entity.ScheduleManagementChannelMember;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MyWorkSpaceInfoDto {
    private Long workSpaceSeq;

    public static List<MyWorkSpaceInfoDto> of(List<ScheduleManagementChannelMember> workSpaceList) {
        return workSpaceList.stream()
                .map(member -> MyWorkSpaceInfoDto.builder()
                        .workSpaceSeq(member.getWorkSpaceSeq())
                        .build())
                .toList();
    }
}