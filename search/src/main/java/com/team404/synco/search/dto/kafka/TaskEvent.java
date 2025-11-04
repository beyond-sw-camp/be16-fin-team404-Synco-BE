package com.team404.synco.search.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
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
}

