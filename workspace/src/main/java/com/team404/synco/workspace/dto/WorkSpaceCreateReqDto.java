package com.team404.synco.workspace.dto;

import com.team404.synco.common.constant.WorkSpaceType;
import com.team404.synco.workspace.entity.WorkSpace;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
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
