package com.team404.synco.alarm.dto;

import com.team404.synco.alarm.entity.Alarm;
import com.team404.synco.member.entity.Member;
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

    public static AlarmResDto fromEntity(Alarm alarm){
        return AlarmResDto.builder()
                .receiverId(alarm.getMember().getMemberId())
                .sender("SYSTEM")
                .alarmType(alarm.getAlarmType())
                .message(alarm.getMessage())
                .build();
    }

    public Alarm toEntity(Member member, AlarmResDto alarmResDto){
        return Alarm.builder()
                .alarmType(alarmResDto.getAlarmType())
                .member(member)
                .build();
    }
}
