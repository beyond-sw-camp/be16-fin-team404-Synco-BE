package com.team404.synco.virtualmeeting.dto.recording;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordingSummaryResponseDto {
    private String recordingId;
    private String roomId;
    private String roomName;
    private String recordingName;
    private String summaryTitle;
    private String summaryContent;
    private LocalDateTime summaryCreatedAt;
    private Long durationSeconds;
    private Integer participantCount;
}
