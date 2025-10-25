package com.team404.synco.virtualmeeting.dto.Room;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoomSessionResDto {
    private Long roomId;
    private String token;
}
