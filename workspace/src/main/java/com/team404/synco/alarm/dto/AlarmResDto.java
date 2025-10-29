package com.team404.synco.alarm.dto;

import com.team404.synco.alarm.entity.Alarm;
import com.team404.synco.member.entity.Member;
import com.team404.synco.workspace.entity.WorkSpace;
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
    private Long workSpaceSeq;
    private LocalDateTime time;

    public static AlarmResDto of(String receiverId, String alarmType, String message, Long workSpaceSeq){
        return AlarmResDto.builder()
                .receiverId(receiverId)
                .sender("SYSTEM")
                .alarmType(alarmType)
                .message(message)
                .workSpaceSeq(workSpaceSeq)
                .build();
    }

    public static AlarmResDto fromEntity(Alarm alarm){
        return AlarmResDto.builder()
                .receiverId(alarm.getMember().getMemberId())
                .sender("SYSTEM")
                .alarmType(alarm.getAlarmType())
                .message(alarm.getMessage())
                .workSpaceSeq(alarm.getWorkSpace().getWorkSpaceSeq())
                .build();
    }

    public Alarm toEntity(Member member, WorkSpace workSpace, AlarmResDto alarmResDto){
        return Alarm.builder()
                .alarmType(alarmResDto.getAlarmType())
                .message(alarmResDto.getMessage())
                .member(member)
                .workSpace(workSpace)
                .build();
    }
}
