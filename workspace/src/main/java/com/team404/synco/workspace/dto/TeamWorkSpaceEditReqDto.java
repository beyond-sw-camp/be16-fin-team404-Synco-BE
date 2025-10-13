package com.team404.synco.workspace.dto;

import jakarta.validation.constraints.NotEmpty;
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
    @NotEmpty(message = "워크스페이스 이름을 입력해주세요.")
    private String workSpaceName;
    private MultipartFile workSpaceThumbnailImage;
}
