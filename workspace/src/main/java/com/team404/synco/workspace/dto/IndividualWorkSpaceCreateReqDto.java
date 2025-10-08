package com.team404.synco.workspace.dto;

import com.team404.synco.common.constant.WorkSpaceType;
import com.team404.synco.member.entity.Member;
import com.team404.synco.workspace.entity.WorkSpace;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class IndividualWorkSpaceCreateReqDto {
    @NotEmpty
    private String workSpaceName;
    private String workSpaceThumbnailImage;
    private List<Long> memberList;

    public WorkSpace toEntity(Member member){
        return WorkSpace.builder()
                .member(member)
                .workSpaceName(this.workSpaceName)
                .workSpaceThumbnailImageUrl(this.workSpaceThumbnailImage)
                .workSpaceType(WorkSpaceType.INDIVIDUAL)
                .build();
    }
}
