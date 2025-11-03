package com.team404.synco.member.dto;

import com.team404.synco.common.constant.ActiveStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberStatusResDto {
    private String memberId;
    private Long memberSeq;
    private ActiveStatus activeStatus;

    public static MemberStatusResDto of(String memberId, Long memberSeq, ActiveStatus activeStatus){
        return MemberStatusResDto.builder()
                .memberId(memberId)
                .memberSeq(memberSeq)
                .activeStatus(activeStatus)
                .build();
    }
}
