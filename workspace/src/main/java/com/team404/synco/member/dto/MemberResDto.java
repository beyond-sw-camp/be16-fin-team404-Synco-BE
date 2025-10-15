package com.team404.synco.member.dto;

import com.team404.synco.common.constant.ActiveStatus;
import com.team404.synco.member.entity.Member;
import lombok.*;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberResDto {
    private String id;
    private String name;
    private String email;
    private String statusMessage;
    private String profileImageUrl;
    private String telNo;
    private ActiveStatus activeStatus;
    private LocalDate birthDate;

    public static MemberResDto fromEntity(Member member) {
        return MemberResDto.builder()
                .id(member.getMemberId())
                .name(member.getName())
                .email(member.getEmail())
                .profileImageUrl(member.getProfileImageUrl())
                .statusMessage(member.getStatusMessage())
                .telNo(member.getTelNo())
                .activeStatus(member.getActiveStatus())
                .birthDate(member.getBirthDate())
                .build();
    }
}
