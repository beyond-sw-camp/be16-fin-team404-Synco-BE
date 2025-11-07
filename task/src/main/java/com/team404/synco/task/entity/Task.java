package com.team404.synco.task.entity;

import com.team404.synco.task.constant.TaskStatus;
import com.team404.synco.task.dto.request.TaskUpdateReqDto;
import com.team404.synco.task.dto.request.PersonalTaskUpdateReqDto;
import com.team404.synco.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Task extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskSeq;
    @Column(nullable = false)
    private String taskTitle;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String taskContent;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskStatus taskStatus;
    @Column(nullable = false)
    private LocalDate startDate;
    @Column(nullable = false)
    private LocalDate endDate;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pic_member_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private ScheduleManagementChannelMember picMemberSeq;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Board board;
    @Builder.Default
    @OneToMany(mappedBy = "task", orphanRemoval = true)
    private List<Comment> commentList = new ArrayList<>();

    public void updateTask(TaskUpdateReqDto taskUpdateReqDto, ScheduleManagementChannelMember picMemberSeq, Board board) {
        this.taskTitle = taskUpdateReqDto.getTaskTitle();
        this.taskContent = taskUpdateReqDto.getTaskContent();
        this.startDate = taskUpdateReqDto.getStartDate();
        this.endDate = taskUpdateReqDto.getEndDate();
        this.picMemberSeq = picMemberSeq;
        this.taskStatus = taskUpdateReqDto.getTaskStatus();
        this.board = board;
    }

    public void updateTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public void updateBoard(Board board) {
        this.board = board;
    }

    // 개인 스케줄 수정용 메서드 (board는 null로 유지)
    public void updatePersonalTask(PersonalTaskUpdateReqDto updateReqDto) {
        this.taskTitle = updateReqDto.getTaskTitle();
        this.taskContent = updateReqDto.getTaskContent();
        this.taskStatus = updateReqDto.getTaskStatus();
        this.startDate = updateReqDto.getStartDate();
        this.endDate = updateReqDto.getEndDate();
    }
}
