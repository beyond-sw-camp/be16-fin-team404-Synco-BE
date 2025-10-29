package com.team404.synco.virtualmeeting.dto.Room;

import com.team404.synco.virtualmeeting.entity.Room;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoomEndedListDto {
    private Long roomId;
    private String roomName;
    private String roomDescription;
    private Integer activeUserCount;
    private Long hostId;

    public static RoomEndedListDto fromEntity(Room room){
        return RoomEndedListDto.builder()
                .roomId(room.getRoomSeq())
                .roomName(room.getRoomName())
                .roomDescription(room.getRoomDescription())
                .activeUserCount(room.getRoomParticipantList().size())
                .hostId(room.getHostId())
                .build();
    }
}
