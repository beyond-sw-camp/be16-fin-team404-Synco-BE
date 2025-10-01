package com.team404.synco.workspace.dto;

import com.team404.synco.common.constant.WorkSpaceType;
import com.team404.synco.workspace.entity.WorkSpace;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class WorkSpaceCreateReqDto {
    private String workSpaceName;
    private String workSpaceThumbnailImageUrl;

    public WorkSpace toEntity(WorkSpaceType workSpaceType){
        return WorkSpace.builder()
                .workSpaceName(this.workSpaceName)
                .workSpaceThumbnailImageUrl(this.workSpaceThumbnailImageUrl)
                .workSpaceType(workSpaceType)
                .build();
    }
}
