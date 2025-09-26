package com.team404.synco.task.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecordingSummary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recordingSummarySeq;
    @Column(nullable = false)
    private String recordingSummaryTitle;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String recordingSummaryContent;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "virtual_meeting_channel_member_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private VirtualMeetingChannelMember virtualMeetingChannelMember;

}
