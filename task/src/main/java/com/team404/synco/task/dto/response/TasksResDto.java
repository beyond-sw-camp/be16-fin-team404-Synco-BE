package com.team404.synco.task.dto.response;

import com.team404.synco.task.constant.TaskStatus;
import com.team404.synco.task.entity.Task;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class TasksResDto {
    private String taskStatusDescription;
    private List<TaskResDto> taskResDtoList;

    @Getter
    @Builder
    public static class TaskResDto {
        private long taskSeq;
        private String taskTitle;
        private TaskStatus taskStatus;
        private LocalDate startDate;
        private LocalDate endDate;
        private long picMemberSeq;
        private String picMemberName;
        private String picMemberProfileImageUrl;

        public static TaskResDto fromEntity(Task task) {
            return TaskResDto.builder()
                    .taskSeq(task.getTaskSeq())
                    .taskTitle(task.getTaskTitle())
                    .taskStatus(task.getTaskStatus())
                    .startDate(task.getStartDate())
                    .endDate(task.getEndDate())
                    .picMemberSeq(task.getPicMemberSeq().getMemberSeq())
                    .picMemberName("") // member 이름 필요하면 추가
                    .picMemberProfileImageUrl("") // member 프로필 필요하면 추가
                    .build();
        }

    }

    public static TasksResDto fromEntity(TaskStatus taskStatus, List<Task> tasks) {
        List<TaskResDto> dtoList = tasks.stream()
                .map(TaskResDto::fromEntity)
                .toList();
        return TasksResDto.builder()
                .taskStatusDescription(taskStatus.getDisplayName())
                .taskResDtoList(dtoList)
                .build();
    }
}
