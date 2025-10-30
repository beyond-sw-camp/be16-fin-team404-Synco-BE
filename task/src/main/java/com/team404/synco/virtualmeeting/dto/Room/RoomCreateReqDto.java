package com.team404.synco.virtualmeeting.dto.Room;

import com.team404.synco.virtualmeeting.entity.Room;
import com.team404.synco.virtualmeeting.entity.VirtualMeetingChannel;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RoomCreateReqDto {
    private Long workSpaceSeq;
    private String roomName;
    private String description;
    private List<Long> alarmMemberList;

    public Room toEntity(Long hostId, VirtualMeetingChannel channel){
        return Room.builder()
                .roomName(this.roomName)
                .roomDescription(this.description)
                .hostId(hostId)
                .virtualMeetingChannel(channel)
                .build();
    }
}
