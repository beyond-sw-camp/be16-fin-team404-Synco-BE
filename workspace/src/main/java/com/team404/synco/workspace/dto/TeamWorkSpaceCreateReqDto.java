package com.team404.synco.workspace.dto;

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
}
