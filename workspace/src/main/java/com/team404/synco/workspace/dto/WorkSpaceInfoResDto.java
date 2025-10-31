package com.team404.synco.workspace.dto;

import com.team404.synco.workspace.entity.WorkSpace;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class WorkSpaceInfoResDto {
    private Long workSpaceSeq;
    private String workSpaceName;
    private String thumbnailImageUrl;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public static WorkSpaceInfoResDto fromEntity(WorkSpace workSpace) {
        return WorkSpaceInfoResDto.builder()
                .workSpaceSeq(workSpace.getWorkSpaceSeq())
                .workSpaceName(workSpace.getWorkSpaceName())
                .thumbnailImageUrl(workSpace.getWorkSpaceThumbnailImageUrl())
                .startDate(workSpace.getStartDate())
                .endDate(workSpace.getEndDate())
                .build();
    }
}