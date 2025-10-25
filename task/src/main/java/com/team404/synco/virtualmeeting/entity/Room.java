package com.team404.synco.virtualmeeting.entity;

import com.team404.synco.common.constant.RoomStatus;
import com.team404.synco.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_seq")
    private Long roomSeq;

    @Column(name = "room_sid", length = 64)
    private String roomSid;

    @Column(name = "room_name", length = 255)
    private String roomName;

    @Column(name = "room_description", length = 500)
    private String roomDescription;

    @Column(name = "host_id", nullable = false)
    private Long hostId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 16, nullable = false)
    @Builder.Default
    private RoomStatus status = RoomStatus.WAITING;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    // 관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_seq", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private VirtualMeetingChannel virtualMeetingChannel;

    @Builder.Default
    @OneToMany(mappedBy = "room", orphanRemoval = true)
    private Set<RoomParticipant> roomParticipantList = new HashSet<>();

    @OneToOne(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private Recording recording;

    // 비즈니스 메서드
    public void startRoom() {
        this.status = RoomStatus.IN_SESSION;
        this.startedAt = LocalDateTime.now();
    }

    public void endRoom() {
        this.status = RoomStatus.ENDED;
        this.endedAt = LocalDateTime.now();
    }
}
