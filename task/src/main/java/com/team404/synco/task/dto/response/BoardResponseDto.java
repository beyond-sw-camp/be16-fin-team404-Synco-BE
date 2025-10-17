package com.team404.synco.task.dto.response;

import com.team404.synco.task.common.domain.MemberInfo;
import com.team404.synco.task.entity.Board;
import com.team404.synco.task.entity.Task;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class BoardResponseDto {
    private long boardSeq;
    private String boardName;
    private long orders;
    private String colors;
    private List<TaskResponseDto> taskResponseDtoList;

    public static BoardResponseDto fromEntity(final Board board, final Map<Long, MemberInfo> memberInfoMap) {
        return BoardResponseDto.builder()
                .boardSeq(board.getBoardSeq())
                .boardName(board.getBoardName())
                .orders(board.getOrders())
                .colors(board.getColors())
                .taskResponseDtoList(board.getTaskList().stream().map(task -> {
                    final MemberInfo memberInfo = memberInfoMap.get(task.getTaskSeq());
                    return TaskResponseDto.fromEntity(task, memberInfo.getMemberName(), memberInfo.getMemberProfileImageUrl());
                }).toList()).build();
    }

    @Getter
    @Builder
    public static class TaskResponseDto {
        private long taskSeq;
        private String taskTitle;
        private LocalDate startDate;
        private LocalDate endDate;
        private long picMemberSeq;
        private String picMemberName;
        private String picMemberProfileImageUrl;

        public static TaskResponseDto fromEntity(final Task task, final String picMemberName, final String picMemberProfileImageUrl) {
            return TaskResponseDto.builder()
                    .taskSeq(task.getTaskSeq())
                    .taskTitle(task.getTaskTitle())
                    .startDate(task.getStartDate())
                    .endDate(task.getEndDate())
                    .picMemberSeq(task.getPicMemberSeq().getScheduleManagementChannelMemberSeq())
                    .picMemberName(picMemberName)
                    .picMemberProfileImageUrl(picMemberProfileImageUrl).build();

        }
    }
}
