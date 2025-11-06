package com.team404.synco.member.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class RedirectDto {
    private String code;
    private String state;
}