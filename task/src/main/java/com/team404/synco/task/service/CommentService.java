package com.team404.synco.task.service;

import com.team404.synco.common.component.MemberRedisComponent;
import com.team404.synco.common.constant.dto.AlarmResDto;
import com.team404.synco.common.service.RedisEventPublisher;
import com.team404.synco.task.dto.request.CommentCreateReqDto;
import com.team404.synco.task.dto.request.CommentUpdateReqDto;
import com.team404.synco.task.dto.response.CommentResDto;
import com.team404.synco.task.entity.Comment;
import com.team404.synco.task.entity.ScheduleManagementChannelMember;
import com.team404.synco.task.entity.Task;
import com.team404.synco.task.repository.CommentRepository;
import com.team404.synco.task.repository.ScheduleManagementChannelMemberRepository;
import com.team404.synco.task.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {
    
    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final ScheduleManagementChannelMemberRepository scheduleManagementChannelMemberRepository;
    private final MemberRedisComponent memberRedisComponent;
    private final RedisEventPublisher redisEventPublisher;
    
    // 댓글 생성 (일반 댓글 또는 대댓글)
    public Long createComment(Long memberSeq, Long taskSeq, CommentCreateReqDto commentCreateReqDto) {
        Task task = taskRepository.findById(taskSeq)
                .orElseThrow(() -> new EntityNotFoundException("업무를 찾을 수 없습니다."));
        
        ScheduleManagementChannelMember member = scheduleManagementChannelMemberRepository
                .findByMemberSeqAndWorkSpaceSeq(memberSeq, task.getPicMemberSeq().getWorkSpaceSeq())
                .orElseThrow(() -> new EntityNotFoundException("해당 워크스페이스에 참여하지 않은 사용자입니다."));
        
        if (commentCreateReqDto.getParentCommentSeq() != null) {
            Comment parentComment = commentRepository.findById(commentCreateReqDto.getParentCommentSeq())
                    .orElseThrow(() -> new EntityNotFoundException("부모 댓글을 찾을 수 없습니다."));
            
            if (!parentComment.getTask().getTaskSeq().equals(taskSeq)) {
                throw new IllegalArgumentException("부모 댓글과 다른 업무입니다.");
            }
        }
        
        Comment comment = commentCreateReqDto.toEntity(member, task);

        // 담당자 및 답글 작성자에게 알림 전송
        String name = memberRedisComponent.getMemberName(comment.getScheduleManagementChannelMember().getMemberSeq());
        String workSpaceName = memberRedisComponent.getWorkSpaceName(task.getPicMemberSeq().getWorkSpaceSeq());

        // 답글이면
        AlarmResDto alarmResDto = null;
        if(comment.getParentCommentSeq() != null){
            alarmResDto = AlarmResDto.of(String.valueOf(task.getPicMemberSeq().getMemberSeq()),
                    "alarm-task", "[댓글 등록] " + workSpaceName + "프로젝트의 " + task.getTaskTitle() + "업무에 " + name + "님이 댓글을 달았습니다.",
                    task.getPicMemberSeq().getWorkSpaceSeq(), task.getTaskSeq());
            redisEventPublisher.publish("alarm-task", alarmResDto);
            alarmResDto = AlarmResDto.of(String.valueOf(task.getPicMemberSeq().getMemberSeq()),
                    "alarm-task", "[댓글 등록] " + workSpaceName + "프로젝트의 " + task.getTaskTitle() + "업무에 " + name + "님이 댓글을 달았습니다.",
                    comment.getScheduleManagementChannelMember().getMemberSeq(), task.getTaskSeq());
            redisEventPublisher.publish("alarm-task", alarmResDto);
        } else {
            alarmResDto = AlarmResDto.of(String.valueOf(task.getPicMemberSeq().getMemberSeq()),
                    "alarm-task", "[댓글 등록] " + workSpaceName + "프로젝트의 " + task.getTaskTitle() + "업무에 " + name + "님이 댓글을 달았습니다.",
                    task.getPicMemberSeq().getWorkSpaceSeq(), task.getTaskSeq());
            redisEventPublisher.publish("alarm-task", alarmResDto);
        }
        return commentRepository.save(comment).getCommentSeq();
    }
    
    // 댓글 수정
    public void updateComment(Long memberSeq, Long commentSeq, CommentUpdateReqDto commentUpdateReqDto) {
        Comment comment = commentRepository.findById(commentSeq)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));
        
        if (!memberSeq.equals(comment.getScheduleManagementChannelMember().getMemberSeq())) {
            throw new ForbiddenException("댓글 작성자만 수정할 수 있습니다.");
        }
        
        comment.updateComment(commentUpdateReqDto);
    }
    
    // 댓글 페이징 조회 (일반 댓글 + 대댓글)
    @Transactional(readOnly = true)
    public Page<CommentResDto> getCommentsByTaskSeq(Long memberSeq, Long taskSeq, Pageable pageable) {
        Task task = taskRepository.findById(taskSeq)
                .orElseThrow(() -> new EntityNotFoundException("업무를 찾을 수 없습니다."));
        
        scheduleManagementChannelMemberRepository
                .findByMemberSeqAndWorkSpaceSeq(memberSeq, task.getPicMemberSeq().getWorkSpaceSeq())
                .orElseThrow(() -> new EntityNotFoundException("해당 워크스페이스에 참여하지 않은 사용자입니다."));
        
        Page<Comment> parentComments = commentRepository.findParentCommentsByTaskSeq(taskSeq, pageable);
        
        return parentComments.map(parentComment -> {
            List<Comment> replies = commentRepository.findRepliesByParentCommentSeq(parentComment.getCommentSeq());
            
            String parentMemberName = memberRedisComponent.getMemberName(parentComment.getScheduleManagementChannelMember().getMemberSeq());
            String parentMemberProfileUrl = memberRedisComponent.getMemberProfileUrl(parentComment.getScheduleManagementChannelMember().getMemberSeq());
            
            List<CommentResDto> replyDtos = replies.stream()
                    .map(reply -> {
                        String replyMemberName = memberRedisComponent.getMemberName(reply.getScheduleManagementChannelMember().getMemberSeq());
                        String replyMemberProfileUrl = memberRedisComponent.getMemberProfileUrl(reply.getScheduleManagementChannelMember().getMemberSeq());
                        return CommentResDto.fromEntity(reply, replyMemberName, replyMemberProfileUrl);
                    })
                    .toList();
            
            return CommentResDto.fromEntityWithReplies(parentComment, parentMemberName, parentMemberProfileUrl, replyDtos);
        });
    }

    // 댓글 삭제 (대댓글도 함께 삭제)
    public void deleteComment(Long memberSeq, Long commentSeq) {
        Comment comment = commentRepository.findById(commentSeq)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));

        if (!memberSeq.equals(comment.getScheduleManagementChannelMember().getMemberSeq())) {
            throw new ForbiddenException("댓글 작성자만 삭제할 수 있습니다.");
        }

        List<Comment> replies = commentRepository.findRepliesByParentCommentSeq(commentSeq);
        commentRepository.deleteAll(replies);

        commentRepository.delete(comment);
    }


}
