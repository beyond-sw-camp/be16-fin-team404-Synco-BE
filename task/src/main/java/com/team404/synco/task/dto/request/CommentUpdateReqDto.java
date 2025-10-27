package com.team404.synco.task.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentUpdateReqDto {
    @NotEmpty
    private String commentContent;
}
