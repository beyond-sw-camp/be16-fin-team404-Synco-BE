package com.team404.synco.drive.dto;

import com.team404.synco.common.constant.WorkSpaceType;
import com.team404.synco.drive.entity.DriveChannel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class DriveCreateReqDto {
    private WorkSpaceType workSpaceType;
    private String workSpaceName;
    private Long workSpaceReq;

    public DriveChannel toEntity(){
        return DriveChannel.builder()
                .workSpaceType(this.workSpaceType)
                .workspaceName(this.workSpaceName)
                .workspaceSeq(this.workSpaceReq)
                .build();
    }
}
