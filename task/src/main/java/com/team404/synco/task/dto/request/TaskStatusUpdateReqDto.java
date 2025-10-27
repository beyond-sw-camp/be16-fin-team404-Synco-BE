package com.team404.synco.task.dto.request;

import com.team404.synco.task.constant.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TaskStatusUpdateReqDto {
    @NotNull
    private TaskStatus taskStatus;
}
