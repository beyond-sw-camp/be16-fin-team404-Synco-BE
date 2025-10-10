package com.team404.synco.member.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NaverProfileDto {
    private Response response;

    @Getter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response{
        private String id;
        private String name;
        private String email;
        private String profile_image;
    }
}
