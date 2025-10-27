package com.team404.synco.virtualmeeting.entity;

import com.team404.synco.common.constant.RecordingStatus;
import com.team404.synco.common.entity.BaseEntity;
import jakarta.persistence.*;
import livekit.LivekitEgress;
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

    @Column(name = "filename", columnDefinition = "TEXT")
    private String filename;

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

    public static Recording fromFileInfo(LivekitEgress.FileInfo fileInfo, Room room) {
        return Recording.builder()
                .filename(fileInfo.getFilename())
                .outputUrl(fileInfo.getLocation())
                .fileSizeBytes(fileInfo.getSize())
                .durationMs(fileInfo.getDuration())
                .room(room)
                .build();
    }
}
