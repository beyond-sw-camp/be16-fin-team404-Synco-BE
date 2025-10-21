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
public class ActiveRoomResponseDto {
    private String roomId;
    private String roomName;
    private String roomStatus;
    private Long channelSeq;
    private Long createdByMemberSeq;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private Integer participantCount;
    private List<ParticipantInfoDto> participants;
}
