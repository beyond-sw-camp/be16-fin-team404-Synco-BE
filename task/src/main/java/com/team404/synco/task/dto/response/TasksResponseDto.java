package com.team404.synco.task.dto.response;

import com.team404.synco.common.constant.TaskStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class TasksResponseDto {
    private String taskStatusName;
    private List<TasksResponseDto> tasksResponseDtoList;

    static class TaskResponseDto {
        private long taskSeq;
        private String taskTitle;
        private TaskStatus taskStatus;
        private LocalDate startDate;
        private LocalDate endDate;
        private long picMemberSeq;
        private String picMemberName;
        private String picMemberProfileImageUrl;
    }
}
