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
public class RecordingSummary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recordingSummarySeq;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "transcript", nullable = true, columnDefinition = "LONGTEXT")
    private String transcript;

    // 관계 설정
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recording_seq", nullable = false, unique = true,
                foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Recording recording;

    public void updateSummary(String summary) {
        this.summary = summary;
    }
}
