package com.team404.synco.virtualmeeting.entity;

import com.team404.synco.common.constant.ParticipantStatus;
import com.team404.synco.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = {
    @Index(name = "idx_participant_room", columnList = "roomSeq"),
    @Index(name = "idx_participant_member", columnList = "memberSeq"),
    @Index(name = "idx_participant_status", columnList = "participantStatus")
})
public class RoomParticipant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomParticipantSeq;

    @Column(nullable = false)
    private Long roomSeq;

    @Column(nullable = false)
    private Long memberSeq;

    @Column(nullable = false, unique = true)
    private String participantId; // LiveKit Participant ID

    @Column(nullable = false, unique = true)
    private String participantSid; // LiveKit Participant SID

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ParticipantStatus participantStatus = ParticipantStatus.CONNECTED;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    private LocalDateTime leftAt;

    // 참가자 메타데이터
    @Column(columnDefinition = "TEXT")
    private String participantMetadata; // JSON 형태의 메타데이터

    // 오디오/비디오 상태
    @Builder.Default
    private Boolean isMuted = false;

    @Builder.Default
    private Boolean isVideoEnabled = true;

    @Builder.Default
    private Boolean isScreenSharing = false;

    // 연결 품질 정보
    private String connectionQuality; // EXCELLENT, GOOD, FAIR, POOR

    // 관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roomSeq", insertable = false, updatable = false,
                foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Room room;

    // 비즈니스 메서드
    public void connect() {
        this.participantStatus = ParticipantStatus.CONNECTED;
        this.joinedAt = LocalDateTime.now();
    }

    public void disconnect() {
        this.participantStatus = ParticipantStatus.DISCONNECTED;
        this.leftAt = LocalDateTime.now();
    }

    public void reconnect() {
        this.participantStatus = ParticipantStatus.RECONNECTING;
    }

    public void toggleMute() {
        this.isMuted = !this.isMuted;
    }

    public void toggleVideo() {
        this.isVideoEnabled = !this.isVideoEnabled;
    }

    public void toggleScreenSharing() {
        this.isScreenSharing = !this.isScreenSharing;
    }

    public void updateConnectionQuality(String quality) {
        this.connectionQuality = quality;
    }

    public void updateParticipantMetadata(String metadata) {
        this.participantMetadata = metadata;
    }

    public boolean isConnected() {
        return this.participantStatus == ParticipantStatus.CONNECTED;
    }

    public boolean isDisconnected() {
        return this.participantStatus == ParticipantStatus.DISCONNECTED;
    }
}
