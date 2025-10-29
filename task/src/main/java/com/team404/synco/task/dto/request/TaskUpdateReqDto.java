package com.team404.synco.task.dto.request;

import com.team404.synco.task.constant.TaskStatus;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class TaskUpdateReqDto {
    @NotEmpty
    private TaskStatus taskStatus;
    @NotEmpty
    private String taskTitle;
    @NotEmpty
    private String taskContent;
    @NotEmpty
    private LocalDate startDate;
    @NotEmpty
    private LocalDate endDate;
    @NotEmpty
    private long picMemberSeq;
    private Long boardSeq;
}

