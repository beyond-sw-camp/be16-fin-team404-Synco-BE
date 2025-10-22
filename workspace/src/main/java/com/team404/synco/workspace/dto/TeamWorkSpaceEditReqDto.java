package com.team404.synco.workspace.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TeamWorkSpaceEditReqDto {
    private Long workSpaceSeq;
    private String workSpaceName;
    private MultipartFile workSpaceThumbnailImage;
}
