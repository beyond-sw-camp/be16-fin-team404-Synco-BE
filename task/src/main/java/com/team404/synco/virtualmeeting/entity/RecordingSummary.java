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
    @Column(name = "recording_summary_seq")
    private Long recordingSummarySeq;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    // 관계 설정
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recording_seq", nullable = false, unique = true,
                foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Recording recording;
}
