package com.team404.synco.task.dto.request;

import com.team404.synco.task.constant.TaskStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class PersonalTaskUpdateReqDto {
    @NotEmpty(message = "제목은 필수입니다")
    private String taskTitle;
    
    @NotEmpty(message = "내용은 필수입니다")
    private String taskContent;
    
    @NotNull(message = "상태는 필수입니다")
    private TaskStatus taskStatus;
    
    @NotNull(message = "시작일은 필수입니다")
    private LocalDate startDate;
    
    @NotNull(message = "종료일은 필수입니다")
    private LocalDate endDate;
}

