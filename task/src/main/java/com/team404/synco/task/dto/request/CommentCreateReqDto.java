package com.team404.synco.task.dto.request;

import com.team404.synco.task.entity.Comment;
import com.team404.synco.task.entity.ScheduleManagementChannelMember;
import com.team404.synco.task.entity.Task;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentCreateReqDto {
    @NotEmpty
    private String commentContent;
    private Long parentCommentSeq; // 대댓글인 경우 부모 댓글 번호

    public Comment toEntity(ScheduleManagementChannelMember scheduleManagementChannelMember, Task task) {
        return Comment.builder()
                .scheduleManagementChannelMember(scheduleManagementChannelMember)
                .task(task)
                .commentContent(this.commentContent)
                .parentCommentSeq(this.parentCommentSeq)
                .build();
    }
}
