package com.team404.synco.workspace.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MyWorkSpaceListResDto {
    private Long memberSeq;
    private List<WorkSpaceInfoDto> workSpaceInfoDtoList;
}