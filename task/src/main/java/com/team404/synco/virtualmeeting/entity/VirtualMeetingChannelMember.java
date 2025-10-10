package com.team404.synco.virtualmeeting.entity;

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
public class VirtualMeetingChannelMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long virtualMeetingChannelMemberSeq;
    @Column(nullable = false)
    private long memberSeq;
    @Column(nullable = false)
    @Builder.Default
    private Authority authority = Authority.SUPER;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "virtual_meeting_channel_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private VirtualMeetingChannel virtualMeetingChannel;
    @Builder.Default
    @OneToMany(mappedBy = "virtualMeetingChannelMember", orphanRemoval = true)
    private List<RecordingSummary> recordingSummaryList = new ArrayList<>();

}
