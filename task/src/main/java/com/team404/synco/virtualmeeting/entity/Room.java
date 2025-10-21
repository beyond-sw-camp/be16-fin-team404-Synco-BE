package com.team404.synco.virtualmeeting.entity;

import com.team404.synco.common.constant.RoomStatus;
import com.team404.synco.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = {
    @Index(name = "idx_room_status", columnList = "roomStatus"),
    @Index(name = "idx_room_channel", columnList = "virtualMeetingChannelSeq")
})
public class Room extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomSeq;

    @Column(nullable = false)
    private Long virtualMeetingChannelSeq;

    @Column(nullable = false, unique = true)
    private String roomId; // LiveKit Room ID

    @Column(nullable = false)
    private String roomName;

    @Column(nullable = false, unique = true)
    private String roomSid; // LiveKit Room SID

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RoomStatus roomStatus = RoomStatus.CREATED;

    @Column(nullable = false)
    private Long createdByMemberSeq;

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    @Builder.Default
    private Boolean recordingEnabled = false;

    // LiveKit Room 메타데이터
    @Column(columnDefinition = "TEXT")
    private String roomMetadata; // JSON 형태의 메타데이터

    // 관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "virtualMeetingChannelSeq", insertable = false, updatable = false, 
                foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private VirtualMeetingChannel virtualMeetingChannel;

    @Builder.Default
    @OneToMany(mappedBy = "room", orphanRemoval = true)
    private List<RoomParticipant> roomParticipantList = new ArrayList<>();

    @OneToOne(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private RoomRecording roomRecording;

    // 비즈니스 메서드
    public void startRoom() {
        this.roomStatus = RoomStatus.ACTIVE;
        this.startedAt = LocalDateTime.now();
    }

    public void endRoom() {
        this.roomStatus = RoomStatus.ENDED;
        this.endedAt = LocalDateTime.now();
    }

    public void failRoom() {
        this.roomStatus = RoomStatus.FAILED;
        this.endedAt = LocalDateTime.now();
    }

    public void updateRoomName(String roomName) {
        this.roomName = roomName;
    }

    public void enableRecording() {
        this.recordingEnabled = true;
    }

    public void disableRecording() {
        this.recordingEnabled = false;
    }

    public void updateRoomMetadata(String metadata) {
        this.roomMetadata = metadata;
    }

    public boolean isActive() {
        return this.roomStatus == RoomStatus.ACTIVE;
    }

    public boolean hasParticipants() {
        return this.roomParticipantList != null && !this.roomParticipantList.isEmpty();
    }
}
