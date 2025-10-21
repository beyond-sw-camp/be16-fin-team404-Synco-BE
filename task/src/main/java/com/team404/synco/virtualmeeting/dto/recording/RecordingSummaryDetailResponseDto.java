package com.team404.synco.virtualmeeting.dto.recording;

import com.team404.synco.virtualmeeting.dto.room.ParticipantInfoDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordingSummaryDetailResponseDto {
    private String recordingId;
    private String roomId;
    private String roomName;
    private String recordingName;
    private String summaryTitle;
    private String summaryContent;
    private LocalDateTime summaryCreatedAt;
    private Long durationSeconds;
    private LocalDateTime startedAt;
    private LocalDateTime stoppedAt;
    private List<ParticipantInfoDto> participants;
    private String recordingProvider;
}
