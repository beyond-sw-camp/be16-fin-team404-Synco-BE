package com.team404.synco.task.dto.response;

import com.team404.synco.task.entity.Board;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class BoardResDto {
    private long boardSeq;
    private String boardName;
    private long orders;
    private String colors;
    private List<TasksResDto.TaskResDto> taskResDtoList;

    public static BoardResDto fromEntity(final Board board) {
        return BoardResDto.builder()
                .boardSeq(board.getBoardSeq())
                .boardName(board.getBoardName())
                .orders(board.getOrders())
                .colors(board.getColors())
                .taskResDtoList(board.getTaskList().stream()
                        .map(TasksResDto.TaskResDto::fromEntity)
                        .toList())
                .build();
    }
}