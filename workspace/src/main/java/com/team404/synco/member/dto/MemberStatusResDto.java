package com.team404.synco.member.dto;

import com.team404.synco.common.constant.ActiveStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberStatusResDto {
    private Long memberSeq;
    private ActiveStatus activeStatus;

    public static MemberStatusResDto of(Long memberSeq, ActiveStatus activeStatus){
        return MemberStatusResDto.builder()
                .memberSeq(memberSeq)
                .activeStatus(activeStatus)
                .build();
    }
}
