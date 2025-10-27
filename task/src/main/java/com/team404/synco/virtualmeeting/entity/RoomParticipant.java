package com.team404.synco.virtualmeeting.entity;

import com.team404.synco.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "room_participant", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"room_seq", "channel_member_seq"}))
public class RoomParticipant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_participant_seq")
    private Long roomParticipantSeq;

    @Column(name = "display_name_at_join", length = 255)
    private String displayNameAtJoin;

    @Column(name = "first_joined_at")
    private LocalDateTime firstJoinedAt;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    // 관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_seq", insertable = false, updatable = false,
                foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_member_seq", nullable = false,
                foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private VirtualMeetingChannelMember virtualMeetingChannelMember;

    public void joinRoom() {
        this.joinedAt = LocalDateTime.now();
    }

    public void leaveRoom() {
        this.leftAt = LocalDateTime.now();
    }
}
