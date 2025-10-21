package com.team404.synco.workspace.dto;

import com.team404.synco.member.entity.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WorkSpaceMemberInfoResDto {
    private Long memberSeq;
    private String name;
    private String profileImageUrl;

    public static WorkSpaceMemberInfoResDto fromEntity(Member member){
        return WorkSpaceMemberInfoResDto.builder()
                .memberSeq(member.getMemberSeq())
                .name(member.getName())
                .profileImageUrl(member.getProfileImageUrl())
                .build();
    }
}
