package com.team404.synco.member.dto;

import com.team404.synco.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberSearchResDto {
    private Long memberSeq;
    private String memberId;
    private String name;
    private String profileImageUrl;
    private String requestStatus = "none";

    public static MemberSearchResDto fromEntity(Member member, String requestStatus) {
        return MemberSearchResDto.builder()
                .memberSeq(member.getMemberSeq())
                .memberId(member.getMemberId())
                .name(member.getName())
                .profileImageUrl(member.getProfileImageUrl())
                .requestStatus(requestStatus)
                .build();
    }

}


