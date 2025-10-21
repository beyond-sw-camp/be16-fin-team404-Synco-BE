package com.team404.synco.task.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoardUpdateReqDto {
    @NotEmpty(message = "보드 이름은 필수입니다.")
    private String boardName;
    private String colors;
}
