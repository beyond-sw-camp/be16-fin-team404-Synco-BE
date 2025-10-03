package com.team404.synco.member.dto;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResDto {
    private String accessToken;
    private String refreshToken;
}
