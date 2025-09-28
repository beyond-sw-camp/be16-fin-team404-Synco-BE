package com.team404.synco.task.entity;

import com.team404.synco.common.constant.Authority;
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
public class ScheduleManagementChannelMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleManagementChannelMemberSeq;
    @Column(nullable = false)
    private long memberSeq;
    @Column(nullable = false)
    @Builder.Default
    private Authority authority = Authority.SUPER;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_management_channel_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private ScheduleManagementChannel scheduleManagementChannel;
    @Builder.Default
    @OneToMany(mappedBy = "scheduleManagementChannelMember")
    private List<Task> taskList = new ArrayList<>();
    @Builder.Default
    @OneToMany(mappedBy = "scheduleManagementChannelMember")
    private List<Comment> commentList = new ArrayList<>();
}
