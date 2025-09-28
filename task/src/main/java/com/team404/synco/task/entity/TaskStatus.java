package com.team404.synco.task.entity;

import com.team404.synco.common.constant.TaskStatusType;
import com.team404.synco.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskStatus extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskStatusSeq;
    @Column(nullable = false)
    private TaskStatusType taskStatusType;
    @Column(nullable = false)
    private String taskStatusName;
    @Column(nullable = false)
    private long orders;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_management_channel_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private ScheduleManagementChannel scheduleManagementChannel;
    @Builder.Default
    @OneToMany(mappedBy = "taskStatus")
    private List<Task> taskList = new ArrayList<>();
}
