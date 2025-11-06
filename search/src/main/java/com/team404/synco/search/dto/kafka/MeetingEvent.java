package com.team404.synco.search.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingEvent {
    private Long recordingSummarySeq;
    private Long recordingSeq;
    private String title;  // Room의 roomName
    private String description;  // Room의 roomDescription
    private String summary;
    private Long workspaceSeq;
    private Long roomSeq;
    private Long hostId;
    private LocalDateTime startedAt;
    private LocalDateTime createdAt;
}

