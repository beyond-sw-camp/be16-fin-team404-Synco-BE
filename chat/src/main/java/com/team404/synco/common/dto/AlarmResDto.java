package com.team404.synco.common.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AlarmResDto {
    private String receiverId;
    private String alarmType;
    private String message;
    private Long workSpaceSeq;
    private Long targetSeq;

    public static AlarmResDto of(String receiverId, String alarmType, String message, Long workSpaceSeq, Long targetSeq){
        return AlarmResDto.builder()
                .receiverId(receiverId)
                .alarmType(alarmType)
                .message(message)
                .workSpaceSeq(workSpaceSeq)
                .targetSeq(targetSeq)
                .build();
    }
}
