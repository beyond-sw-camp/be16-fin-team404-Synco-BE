package com.team404.synco.workspace.dto;

import com.team404.synco.common.constant.WorkSpaceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class DriveCreateReqDto {
    private WorkSpaceType workSpaceType;
    private String workSpaceName;
    private Long workSpaceReq;
}
