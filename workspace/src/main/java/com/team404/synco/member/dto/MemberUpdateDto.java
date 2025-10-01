package com.team404.synco.member.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberUpdateDto {
    private String id;
    private String name;
    private String email;
    private String statusMessage;
    private String profileImageUrl;
    private String telNo;
}
