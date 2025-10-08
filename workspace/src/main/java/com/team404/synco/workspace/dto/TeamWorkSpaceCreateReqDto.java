package com.team404.synco.workspace.dto;

import com.team404.synco.common.constant.WorkSpaceType;
import com.team404.synco.member.entity.Member;
import com.team404.synco.workspace.entity.WorkSpace;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class TeamWorkSpaceCreateReqDto {
    @NotEmpty(message = "워크스페이스 이름을 입력해주세요.")
    private String workSpaceName;
    private MultipartFile workSpaceThumbnailImage;
    private List<Long> memberList;

    public WorkSpace toEntity(Member member, String workSpaceThumbnailImageUrl){
        return WorkSpace.builder()
                .member(member)
                .workSpaceName(this.workSpaceName)
                .workSpaceThumbnailImageUrl(workSpaceThumbnailImageUrl)
                .workSpaceType(WorkSpaceType.TEAM)
                .build();
    }
}
