package com.team404.synco.virtualmeeting.dto.room;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantInfoDto {
    private String participantId;
    private String participantSid;
    private Long memberSeq;
    private String participantName;
    private String participantStatus;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
    private Boolean isMuted;
    private Boolean isVideoEnabled;
    private Boolean isScreenSharing;
    private String connectionQuality;
}
