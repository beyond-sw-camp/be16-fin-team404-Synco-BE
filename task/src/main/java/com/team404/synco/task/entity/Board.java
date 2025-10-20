package com.team404.synco.task.entity;

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
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardSeq;
    @Column(nullable = false)
    private String boardName;
    @Column(nullable = false)
    private long orders;
    @Column(nullable = false)
    private String colors;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_management_channel_member_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private ScheduleManagementChannelMember scheduleManagementChannelMember;
    @Builder.Default
    @OneToMany(mappedBy = "board", orphanRemoval = true)
    private List<Task> taskList = new ArrayList<>();
}
