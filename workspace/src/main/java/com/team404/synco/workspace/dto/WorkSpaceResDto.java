package com.team404.synco.workspace.dto;

import com.team404.synco.common.constant.WorkSpaceType;
import com.team404.synco.workspace.entity.WorkSpace;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WorkSpaceResDto {
    private Long workSpaceSeq;
    private String workSpaceName;
    private WorkSpaceType workSpaceType;
    private String workSpaceOwner;
    private String workSpaceThumbnailImage;

    public static WorkSpaceResDto fromEntity(WorkSpace workSpace){
        return WorkSpaceResDto.builder()
                .workSpaceSeq(workSpace.getWorkSpaceSeq())
                .workSpaceName(workSpace.getWorkSpaceName())
                .workSpaceType(workSpace.getWorkSpaceType())
                .workSpaceOwner(workSpace.getMember().getName())
                .workSpaceThumbnailImage(workSpace.getWorkSpaceThumbnailImageUrl())
                .build();
    }
}
