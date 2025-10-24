package com.team404.synco.virtualmeeting.dto.Room;

import com.team404.synco.virtualmeeting.entity.Room;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoomActiveListDto {
    private Long roomId;
    private String roomName;
    private String roomDescription;
    private Integer activeUserCount;
    private Long hostId;

    public static RoomActiveListDto fromEntity(Room room){
        return RoomActiveListDto.builder()
                .roomId(room.getRoomSeq())
                .roomName(room.getRoomName())
                .roomDescription(room.getRoomDescription())
                .activeUserCount(room.getRoomParticipantList().size())
                .hostId(room.getHostId())
                .build();
    }
}
