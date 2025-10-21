package com.team404.synco.virtualmeeting.entity;

import com.team404.synco.common.constant.RoomRecordingStatus;
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
    @Index(name = "idx_room_recording_status", columnList = "recordingStatus"),
    @Index(name = "idx_room_recording_room", columnList = "roomSeq")
})
public class RoomRecording extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomRecordingSeq;

    @Column(nullable = false)
    private Long roomSeq;

    @Column(nullable = false, unique = true)
    private String recordingId; // LiveKit 또는 외부 녹화 서비스 ID

    @Column(nullable = false)
    private String recordingName;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RoomRecordingStatus recordingStatus = RoomRecordingStatus.RECORDING;

    private Integer durationSeconds; // 녹화 시간 (초)

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime stoppedAt;

    // 녹화 서비스 정보
    @Column(nullable = false)
    private String recordingProvider; // "livekit", "external", "custom"

    private String recordingUrl; // 외부 녹화 서비스 URL (필요시)

    // 요약 관련 필드
    private String summaryTitle; // 요약 제목
    @Column(columnDefinition = "TEXT")
    private String summaryContent; // 요약 내용
    private LocalDateTime summaryCreatedAt; // 요약 생성 시간

    // 관계 설정
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roomSeq", insertable = false, updatable = false,
                foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Room room;

    // 비즈니스 메서드
    public void startRecording() {
        this.recordingStatus = RoomRecordingStatus.RECORDING;
        this.startedAt = LocalDateTime.now();
    }

    public void completeRecording(Integer durationSeconds) {
        this.recordingStatus = RoomRecordingStatus.COMPLETED;
        this.durationSeconds = durationSeconds;
        this.stoppedAt = LocalDateTime.now();
    }

    public void failRecording() {
        this.recordingStatus = RoomRecordingStatus.FAILED;
        this.stoppedAt = LocalDateTime.now();
    }

    public void updateRecordingName(String recordingName) {
        this.recordingName = recordingName;
    }

    public void updateDuration(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public void updateRecordingProvider(String provider, String url) {
        this.recordingProvider = provider;
        this.recordingUrl = url;
    }

    // 요약 관련 메서드
    public void setSummary(String summaryTitle, String summaryContent) {
        this.summaryTitle = summaryTitle;
        this.summaryContent = summaryContent;
        this.summaryCreatedAt = LocalDateTime.now();
    }

    public void updateSummary(String summaryTitle, String summaryContent) {
        this.summaryTitle = summaryTitle;
        this.summaryContent = summaryContent;
        this.summaryCreatedAt = LocalDateTime.now();
    }

    public boolean hasSummary() {
        return this.summaryContent != null && !this.summaryContent.trim().isEmpty();
    }

    public boolean isCompleted() {
        return this.recordingStatus == RoomRecordingStatus.COMPLETED;
    }

    public boolean isRecording() {
        return this.recordingStatus == RoomRecordingStatus.RECORDING;
    }
}
