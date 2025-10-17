package com.team404.synco.task.dto.response;

import com.team404.synco.task.constant.TaskStatus;
import com.team404.synco.task.entity.Task;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class TasksResponseDto {
    private String taskStatusDescription;
    private List<TaskResponseDto> taskResponseDtoList;

    @Getter
    @Builder
    public static class TaskResponseDto {
        private long taskSeq;
        private String taskTitle;
        private TaskStatus taskStatus;
        private LocalDate startDate;
        private LocalDate endDate;
        private long picMemberSeq;
        private String picMemberName;
        private String picMemberProfileImageUrl;

        public static TaskResponseDto fromEntity(Task task) {
            return TaskResponseDto.builder()
                    .taskSeq(task.getTaskSeq())
                    .taskTitle(task.getTaskTitle())
                    .taskStatus(task.getTaskStatus())
                    .startDate(task.getStartDate())
                    .endDate(task.getEndDate())
                    .picMemberSeq(task.getPicMemberSeq().getScheduleManagementChannelMemberSeq())
                    .picMemberName("") // member 이름 필요하면 추가
                    .picMemberProfileImageUrl("") // member 프로필 필요하면 추가
                    .build();
        }

    }

    public static TasksResponseDto fromEntity(TaskStatus taskStatus, List<Task> tasks) {
        List<TaskResponseDto> dtoList = tasks.stream()
                .map(TaskResponseDto::fromEntity)
                .toList();
        return TasksResponseDto.builder()
                .taskStatusDescription(taskStatus.getDisplayName())
                .taskResponseDtoList(dtoList)
                .build();
    }
}
