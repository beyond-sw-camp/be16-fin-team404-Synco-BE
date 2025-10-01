package com.team404.synco.workspace.dto;

import com.team404.synco.common.constant.WorkSpaceType;
import com.team404.synco.member.entity.Member;
import com.team404.synco.workspace.entity.WorkSpace;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class WorkSpaceCreateReqDto {
    private String workSpaceName;
    private MultipartFile workSpaceThumbnailImage;

    public WorkSpace toEntity(Member member, WorkSpaceType workSpaceType, String workSpaceThumbnailImageUrl){
        return WorkSpace.builder()
                .member(member)
                .workSpaceName(this.workSpaceName)
                .workSpaceThumbnailImageUrl(workSpaceThumbnailImageUrl)
                .workSpaceType(workSpaceType)
                .build();
    }
}
