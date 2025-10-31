package com.team404.synco.workspace.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TeamWorkSpaceCreateReqDto {
    @NotEmpty(message = "프로젝트 이름을 입력해주세요.")
    private String workSpaceName;
    private MultipartFile workSpaceThumbnailImage;
    private List<Long> memberList;
    @NotEmpty(message = "프로젝트 시작일을 입력하세요.")
    private LocalDateTime startDate;
    @NotEmpty(message = "프로젝트 종료일을 입력하세요.")
    private LocalDateTime endDate;
}
