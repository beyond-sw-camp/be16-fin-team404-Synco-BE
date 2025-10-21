package com.team404.synco.virtualmeeting.dto.room;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomRequestDto {
    private String roomName;
    private Long channelSeq;
    private List<Long> inviteeMemberSeqs; // 초대할 사람들 (알림용)
}
