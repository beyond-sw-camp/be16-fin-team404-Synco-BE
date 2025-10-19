package com.team404.synco.workspace.dto;

import com.team404.synco.workspace.entity.WorkSpace;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WorkSpaceInfoDto {
    private Long workSpaceSeq;
    private String workSpaceName;
    private String thumbnailImageUrl;

    public static WorkSpaceInfoDto fromEntity(WorkSpace workSpace) {
        return WorkSpaceInfoDto.builder()
                .workSpaceSeq(workSpace.getWorkSpaceSeq())
                .workSpaceName(workSpace.getWorkSpaceName())
                .thumbnailImageUrl(workSpace.getWorkSpaceThumbnailImageUrl())
                .build();
    }
}