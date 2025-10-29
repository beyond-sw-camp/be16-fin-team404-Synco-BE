package com.team404.synco.task.dto.response;

import com.team404.synco.task.constant.TaskStatus;
import com.team404.synco.task.entity.Task;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class PersonalTaskResDto {
    private Long taskSeq;
    private String taskTitle;
    private String taskContent;
    private TaskStatus taskStatus;
    private LocalDate startDate;
    private LocalDate endDate;

    public static PersonalTaskResDto fromEntity(Task task) {
        return PersonalTaskResDto.builder()
                .taskSeq(task.getTaskSeq())
                .taskTitle(task.getTaskTitle())
                .taskContent(task.getTaskContent())
                .taskStatus(task.getTaskStatus())
                .startDate(task.getStartDate())
                .endDate(task.getEndDate())
                .build();
    }
}

