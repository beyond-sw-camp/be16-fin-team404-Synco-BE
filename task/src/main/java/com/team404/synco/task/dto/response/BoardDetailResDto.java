package com.team404.synco.task.dto.response;

import com.team404.synco.task.entity.Board;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BoardDetailResDto {
    private long boardSeq;
    private String boardName;
    private long orders;
    private String colors;
    private long scheduleManagementChannelMemberSeq;
    private List<TaskDetailResDto> taskResDtoList;

    public static BoardDetailResDto fromEntity(Board board) {
        return BoardDetailResDto.builder()
                .boardSeq(board.getBoardSeq())
                .boardName(board.getBoardName())
                .orders(board.getOrders())
                .colors(board.getColors())
                .scheduleManagementChannelMemberSeq(board.getScheduleManagementChannelMember().getScheduleManagementChannelMemberSeq())
                .taskResDtoList(board.getTaskList().stream()
                        .map(TaskDetailResDto::fromEntity)
                        .toList())
                .build();
    }
}
