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
public class RecentRoomResponseDto {
    private String roomId;
    private String roomName;
    private String roomStatus;
    private Long channelSeq;
    private Long createdByMemberSeq;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Integer participantCount;
    private Boolean hasRecording;
}
