package com.team404.synco.alarm.dto;

import com.team404.synco.common.constant.AlarmType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class AlarmFindReqDto {
    private AlarmType alarmType;
    private Long workSpaceSeq;
}
