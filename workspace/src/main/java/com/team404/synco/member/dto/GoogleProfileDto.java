package com.team404.synco.member.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)

public class GoogleProfileDto {
    private String sub;
    private String email;
    private String picture;
    private String name;
}