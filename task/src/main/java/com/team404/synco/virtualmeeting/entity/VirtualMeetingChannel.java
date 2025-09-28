package com.team404.synco.virtualmeeting.entity;

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
public class VirtualMeetingChannel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long virtualMeetingChannelSeq;
    @Column(nullable = false)
    private String virtualMeetingChannelName;
    @Column(nullable = false)
    private long workSpaceSeq;
    @Builder.Default
    @OneToMany(mappedBy = "virtualMeetingChannel")
    private List<VirtualMeetingChannelMember> virtualMeetingChannelMemberList = new ArrayList<>();

}
