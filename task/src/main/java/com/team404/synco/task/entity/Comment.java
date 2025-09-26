package com.team404.synco.task.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@Builder
@AllArgsConstructor
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentSeq;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String commentContent;
    @Column(nullable = false)
    private long parentCommentSeq;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_management_channel_member_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private ScheduleManagementChannelMember scheduleManagementChannelMember;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private Task task;
}
