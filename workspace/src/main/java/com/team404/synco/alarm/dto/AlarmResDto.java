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
        private Long alarmSeq;
        private String receiverId;
        private String sender;
        private String alarmType;
        private String message;
        private String ynRead;
        private Long workSpaceSeq;
        private Long targetSeq;
        private LocalDateTime time;

    public static AlarmResDto of(String receiverId, String alarmType, String message, Long workSpaceSeq, Long targetSeq){
        return AlarmResDto.builder()
                .receiverId(receiverId)
                .sender("SYSTEM")
                .alarmType(alarmType)
                .message(message)
                .workSpaceSeq(workSpaceSeq)
                .targetSeq(targetSeq)
                .build();
    }

    public static AlarmResDto fromEntity(Alarm alarm){
        return AlarmResDto.builder()
                .alarmSeq(alarm.getAlarmSeq())
                .receiverId(alarm.getMember().getMemberId())
                .sender("SYSTEM")
                .alarmType(alarm.getAlarmType())
                .message(alarm.getMessage())
                .ynRead(alarm.getYnRead())
                .workSpaceSeq(alarm.getWorkSpace().getWorkSpaceSeq())
                .targetSeq(alarm.getTargetSeq())
                .time(alarm.getCreatedAt())
                .build();
    }

    public Alarm toEntity(Member member, WorkSpace workSpace, AlarmResDto alarmResDto){
        return Alarm.builder()
                .alarmType(alarmResDto.getAlarmType())
                .message(alarmResDto.getMessage())
                .targetSeq(alarmResDto.getTargetSeq())
                .member(member)
                .workSpace(workSpace)
                .build();
    }
}
