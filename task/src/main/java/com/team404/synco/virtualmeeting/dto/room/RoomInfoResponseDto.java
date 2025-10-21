package com.team404.synco.virtualmeeting.dto.room;

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
public class RoomInfoResponseDto {
    private String roomId;
    private String roomSid;
    private String roomName;
    private String roomStatus;
    private Long channelSeq;
    private Long createdByMemberSeq;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Boolean recordingEnabled;
    private Integer participantCount;
    private List<ParticipantInfoDto> participants;
}
