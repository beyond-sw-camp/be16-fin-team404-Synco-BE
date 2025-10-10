package com.team404.synco.member.service;

import com.team404.synco.member.dto.AccessTokenDto;
import com.team404.synco.member.dto.NaverProfileDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class NaverService {

    @Value("${oauth.naver.client-id}")
    private String naverClientId;

    @Value("${oauth.naver.client-secret}")
    private String naverClientSecret;

    @Value("${oauth.naver.redirect-uri}")
    private String naverRedirectUri;

    public AccessTokenDto getAccessToken(String code, String state) {
        RestClient restClient = RestClient.create();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", naverClientId);
        params.add("client_secret", naverClientSecret);
        params.add("code", code);
        params.add("state", state); // CSRF 방지 값 (인가 요청 시 전달했던 state)

        ResponseEntity<AccessTokenDto> response = restClient.post()
                .uri("https://nid.naver.com/oauth2.0/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(params)
                .retrieve()
                .toEntity(AccessTokenDto.class);
        log.info("응답 accesstoken JSON {}", response.getBody());
        return response.getBody();
    }

    public NaverProfileDto getNaverProfile(String accessToken) {
        RestClient restClient = RestClient.create();

        ResponseEntity<NaverProfileDto> response = restClient.get()
                .uri("https://openapi.naver.com/v1/nid/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .toEntity(NaverProfileDto.class);
        log.info("profile JSON{}", response.getBody());
        return response.getBody();
    }
}