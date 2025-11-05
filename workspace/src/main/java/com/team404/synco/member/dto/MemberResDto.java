package com.team404.synco.member.dto;

import com.team404.synco.common.constant.ActiveStatus;
import com.team404.synco.member.entity.Member;
import com.team404.synco.common.constant.SocialType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private SocialType socialType;
    private String ynAlarmOffSet;
    private LocalDate birthDate;
    private LocalDateTime createdAt;

    public static MemberResDto fromEntity(Member member) {
        return MemberResDto.builder()
                .id(member.getMemberId())
                .name(member.getName())
                .email(member.getEmail())
                .profileImageUrl(member.getProfileImageUrl())
                .statusMessage(member.getStatusMessage())
                .telNo(member.getTelNo())
                .activeStatus(member.getActiveStatus())
                .socialType(member.getSocialType())
                .ynAlarmOffSet(member.getYnAlarmOffSet())
                .birthDate(member.getBirthDate())
                .createdAt(member.getCreatedAt())
                .build();
    }
}
