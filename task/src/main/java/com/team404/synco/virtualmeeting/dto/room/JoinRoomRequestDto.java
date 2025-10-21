package com.team404.synco.virtualmeeting.dto.room;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinRoomRequestDto {
    private Long memberSeq;
    private String participantName; // 참가자 이름
}
