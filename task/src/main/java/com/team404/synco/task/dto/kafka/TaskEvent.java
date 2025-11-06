package com.team404.synco.task.dto.kafka;

import com.team404.synco.task.entity.Task;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskEvent {
    private Long taskSeq;
    private String taskTitle;
    private String taskContent;
    private String taskStatus;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long workspaceSeq;
    private Long memberSeq;
    private LocalDateTime createdAt;
    
    public static TaskEvent fromEntity(Task task) {
        return TaskEvent.builder()
                .taskSeq(task.getTaskSeq())
                .taskTitle(task.getTaskTitle())
                .taskContent(task.getTaskContent())
                .taskStatus(task.getTaskStatus().name())
                .startDate(task.getStartDate())
                .endDate(task.getEndDate())
                .workspaceSeq(task.getPicMemberSeq().getWorkSpaceSeq())
                .memberSeq(task.getPicMemberSeq().getMemberSeq())
                .createdAt(task.getCreatedAt())
                .build();
    }
}

