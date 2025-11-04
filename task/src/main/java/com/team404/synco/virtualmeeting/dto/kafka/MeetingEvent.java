package com.team404.synco.virtualmeeting.dto.kafka;

import com.team404.synco.virtualmeeting.entity.Recording;
import com.team404.synco.virtualmeeting.entity.RecordingSummary;
import com.team404.synco.virtualmeeting.entity.Room;
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
    private String transcript;
    private Long workspaceSeq;
    private Long roomSeq;
    private Long hostId;
    private LocalDateTime startedAt;
    private LocalDateTime createdAt;
    
    public static MeetingEvent fromEntity(RecordingSummary summary) {
        Recording recording = summary.getRecording();
        Room room = recording.getRoom();
        
        return MeetingEvent.builder()
                .recordingSummarySeq(summary.getRecordingSummarySeq())
                .recordingSeq(recording.getRecordingSeq())
                .title(room.getRoomName())
                .description(room.getRoomDescription())
                .summary(summary.getSummary())
                .transcript(summary.getTranscript())
                .workspaceSeq(room.getVirtualMeetingChannel().getWorkSpaceSeq())
                .roomSeq(room.getRoomSeq())
                .hostId(room.getHostId())
                .startedAt(room.getStartedAt())
                .createdAt(summary.getCreatedAt())
                .build();
    }
}

