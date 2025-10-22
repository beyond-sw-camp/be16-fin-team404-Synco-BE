package com.team404.synco.virtualmeeting.entity;

import com.team404.synco.common.constant.RecordingStatus;
import com.team404.synco.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recording extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recording_seq")
    private Long recordingSeq;

    @Column(name = "egress_id", length = 64)
    private String egressId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 16, nullable = false)
    @Builder.Default
    private RecordingStatus status = RecordingStatus.STARTED;

    @Column(name = "output_url", columnDefinition = "TEXT")
    private String outputUrl;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    // 관계 설정
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_seq", nullable = false, unique = true,
                foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Room room;

    @OneToOne(mappedBy = "recording", cascade = CascadeType.ALL, orphanRemoval = true)
    private RecordingSummary recordingSummary;

    // 비즈니스 메서드
    public void startRecording() {
        this.status = RecordingStatus.STARTED;
        this.startedAt = LocalDateTime.now();
    }

    public void endRecording() {
        this.status = RecordingStatus.ENDED;
        this.endedAt = LocalDateTime.now();
    }

    public void failRecording() {
        this.status = RecordingStatus.FAILED;
        this.endedAt = LocalDateTime.now();
    }

    public void updateOutputUrl(String outputUrl) {
        this.outputUrl = outputUrl;
    }

    public void updateDuration(Long durationMs) {
        this.durationMs = durationMs;
    }

    public void updateFileSize(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public boolean isCompleted() {
        return this.status == RecordingStatus.ENDED;
    }

    public boolean isFailed() {
        return this.status == RecordingStatus.FAILED;
    }
}
