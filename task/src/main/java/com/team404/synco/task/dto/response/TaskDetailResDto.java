package com.team404.synco.task.dto.response;

import com.team404.synco.task.constant.TaskStatus;
import com.team404.synco.task.entity.Task;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class TaskDetailResDto {
    private long taskSeq;
    private String taskTitle;
    private String taskContent;
    private TaskStatus taskStatus;
    private LocalDate startDate;
    private LocalDate endDate;
    private long picMemberSeq;
    private Long boardSeq;
    private String picMemberName;
    private String picMemberProfileImageUrl;

    public static TaskDetailResDto fromEntity(Task task) {
        return TaskDetailResDto.builder()
                .taskSeq(task.getTaskSeq())
                .taskTitle(task.getTaskTitle())
                .taskContent(task.getTaskContent())
                .taskStatus(task.getTaskStatus())
                .startDate(task.getStartDate())
                .endDate(task.getEndDate())
                .picMemberSeq(task.getPicMemberSeq().getScheduleManagementChannelMemberSeq())
                .boardSeq(task.getBoard() != null ? task.getBoard().getBoardSeq() : null)
                .picMemberName("") // member 이름 필요하면 추가
                .picMemberProfileImageUrl("") // member 프로필 필요하면 추가
                .build();
    }
}
