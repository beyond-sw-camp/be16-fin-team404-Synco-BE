package com.team404.synco.common.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AlarmResDto {
    private String receiverId;
    private String sender;
    private String alarmType;
    private String message;
    private LocalDateTime time;

    public static AlarmResDto of(String receiverId, String alarmType, String message){
        return AlarmResDto.builder()
                .receiverId(receiverId)
                .sender("SYSTEM")
                .alarmType(alarmType)
                .message(message)
                .build();
    }
}
