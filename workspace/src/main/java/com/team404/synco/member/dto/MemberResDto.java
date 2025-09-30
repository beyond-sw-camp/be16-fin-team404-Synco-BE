package com.team404.synco.workspace.dto;

import com.team404.synco.workspace.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberResDTO {
    private String id;
    private String name;
    private String email;
    private String statusMessage;
    private String profileImageUrl;
    private String telNo;

    public static MemberResDTO fromEntity(Member member) {
        return MemberResDTO.builder()
                .id(member.getMemberId())
                .name(member.getName())
                .email(member.getEmail())
                .profileImageUrl(member.getProfileImageUrl())
                .statusMessage(member.getStatusMessage())
                .telNo(member.getTelNo())
                .build();
    }
}
