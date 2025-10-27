package com.team404.synco.task.dto.response;

import com.team404.synco.task.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CommentResDto {
    private Long commentSeq;
    private String commentContent;
    private Long parentCommentSeq;
    private Long memberSeq;
    private String memberName;
    private String memberProfileImageUrl;
    private LocalDateTime createdAt;
    private List<CommentResDto> replies; // 대댓글 목록

    public static CommentResDto fromEntity(Comment comment, String memberName, String memberProfileImageUrl) {
        return CommentResDto.builder()
                .commentSeq(comment.getCommentSeq())
                .commentContent(comment.getCommentContent())
                .parentCommentSeq(comment.getParentCommentSeq())
                .memberSeq(comment.getScheduleManagementChannelMember().getMemberSeq())
                .memberName(memberName)
                .memberProfileImageUrl(memberProfileImageUrl)
                .createdAt(comment.getCreatedAt())
                .replies(List.of()) // 대댓글은 별도로 처리
                .build();
    }
    
    public static CommentResDto fromEntityWithReplies(Comment comment, String memberName, String memberProfileImageUrl, List<CommentResDto> replies) {
        return CommentResDto.builder()
                .commentSeq(comment.getCommentSeq())
                .commentContent(comment.getCommentContent())
                .parentCommentSeq(comment.getParentCommentSeq())
                .memberSeq(comment.getScheduleManagementChannelMember().getMemberSeq())
                .memberName(memberName)
                .memberProfileImageUrl(memberProfileImageUrl)
                .createdAt(comment.getCreatedAt())
                .replies(replies)
                .build();
    }
}
