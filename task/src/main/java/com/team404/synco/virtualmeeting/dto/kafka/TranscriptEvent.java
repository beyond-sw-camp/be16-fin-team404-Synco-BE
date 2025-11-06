package com.team404.synco.virtualmeeting.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptEvent {
    private Long recordingSeq;
    private String transcript; // STT 변환된 텍스트
    private String outputUrl;  // 원본 영상 URL
}
