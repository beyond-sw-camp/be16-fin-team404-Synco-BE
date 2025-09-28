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
public class ScheduleManagementChannel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleManagementChannelSeq;
    @Column(nullable = false)
    private String scheduleManagementChannelName;
    @Column(nullable = false)
    private long workSpaceSeq;
    @Builder.Default
    @OneToMany(mappedBy = "scheduleManagementChannel")
    private List<TaskStatus> taskStatusList = new ArrayList<>();
    @Builder.Default
    @OneToMany(mappedBy = "scheduleManagementChannel")
    private List<ScheduleManagementChannelMember> scheduleManagementChannelMemberList = new ArrayList<>();

}
