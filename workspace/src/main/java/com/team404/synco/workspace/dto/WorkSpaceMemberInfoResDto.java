package com.team404.synco.workspace.dto;

import com.team404.synco.common.constant.ActiveStatus;
import com.team404.synco.common.constant.Authority;
import com.team404.synco.member.entity.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WorkSpaceMemberInfoResDto {
    private Long memberSeq;
    private String name;
    private ActiveStatus activeStatus;
    private String profileImageUrl;
    private Authority authority;

    public static WorkSpaceMemberInfoResDto of(Member member, Authority authority){
        return WorkSpaceMemberInfoResDto.builder()
                .memberSeq(member.getMemberSeq())
                .name(member.getName())
                .activeStatus(member.getActiveStatus())
                .profileImageUrl(member.getProfileImageUrl())
                .authority(authority)
                .build();
    }
}
