package com.team404.synco.member.dto;

import com.team404.synco.common.constant.ActiveStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ActiveStatusUpdateReqDto {
    private ActiveStatus activeStatus;
}