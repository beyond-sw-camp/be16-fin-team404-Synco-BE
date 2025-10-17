package com.team404.synco.task.dto.request;

import com.team404.synco.task.constant.TaskStatus;
import com.team404.synco.task.entity.Board;
import com.team404.synco.task.entity.ScheduleManagementChannelMember;
import com.team404.synco.task.entity.Task;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Optional;

@Getter
@Builder
public class TaskCreateRequestDto {
    @NotEmpty
    private TaskStatus taskStatus;
    @NotEmpty
    private String taskTitle;
    @NotEmpty
    private String taskContents;
    @NotEmpty
    private String taskStatusDescription;
    @NotEmpty
    private LocalDate startDate;
    @NotEmpty
    private LocalDate endDate;
    @NotEmpty
    private long picMemberSeq;
    private Long boardSeq;

    public Task toEntity(final ScheduleManagementChannelMember scheduleManagementChannelMember, final Optional<Board> board) {
        return Task.builder()
                .taskStatus(this.taskStatus)
                .taskTitle(this.taskTitle)
                .taskContent(this.taskContents)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .picMemberSeq(scheduleManagementChannelMember)
                .board(board.orElse(null))
                .build();
    }
}
