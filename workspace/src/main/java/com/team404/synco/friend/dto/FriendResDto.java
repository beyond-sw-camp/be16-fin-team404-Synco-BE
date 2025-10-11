package com.team404.synco.friend.dto;

import com.team404.synco.common.constant.ActiveStatus;
import com.team404.synco.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FriendResDto {
    private Long memberSeq;
    private String memberId;
    private String name;
    private ActiveStatus activeStatus;

    public static FriendResDto fromEntity(Member member) {
        return FriendResDto.builder()
                .memberSeq(member.getMemberSeq())
                .memberId(member.getMemberId())
                .name(member.getName())
                .activeStatus(member.getActiveStatus())
                .build();
    }
}
