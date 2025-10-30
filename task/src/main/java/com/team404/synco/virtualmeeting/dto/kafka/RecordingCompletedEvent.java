package com.team404.synco.virtualmeeting.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordingCompletedEvent {
    private Long recordingSeq;
    private String egressId;
    private String outputUrl;
    private String filename;
    private Long roomSeq;
}
