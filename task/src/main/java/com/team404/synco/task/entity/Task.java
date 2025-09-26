package com.team404.synco.task.entity;

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
    private long picMemberSeq;
    @Column(nullable = false)
    private LocalDate startDate;
    @Column(nullable = false)
    private LocalDate endDate;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_management_channel_member_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private ScheduleManagementChannelMember scheduleManagementChannelMember;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_status_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private TaskStatus taskStatus;
    @Builder.Default
    @OneToMany(mappedBy = "task")
    private List<Comment> commentList = new ArrayList<>();
}
