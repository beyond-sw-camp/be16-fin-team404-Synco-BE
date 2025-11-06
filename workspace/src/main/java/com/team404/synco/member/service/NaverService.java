package com.team404.synco.member.service;

import com.team404.synco.member.dto.AccessTokenDto;
import com.team404.synco.member.dto.NaverProfileDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class NaverService {

    @Value("${oauth.naver.client-id}")
    private String naverClientId;

    @Value("${oauth.naver.client-secret}")
    private String naverClientSecret;

    @Value("${oauth.naver.redirect-uri}")
    private String naverRedirectUri;

    public AccessTokenDto getAccessToken(String code, String state) {
        try {
            RestClient restClient = RestClient.create();

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", naverClientId);
            params.add("client_secret", naverClientSecret);
            params.add("redirect_uri", naverRedirectUri);
            params.add("code", code);
            params.add("state", state); // CSRF 방지 값 (인가 요청 시 전달했던 state)

            ResponseEntity<AccessTokenDto> response = restClient.post()
                    .uri("https://nid.naver.com/oauth2.0/token")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .body(params)
                    .retrieve()
                    .toEntity(AccessTokenDto.class);
            
            return response.getBody();
        } catch (Exception e) {
            throw new IllegalStateException("네이버 로그인 중 오류가 발생했습니다.", e);
        }
    }

    public NaverProfileDto getNaverProfile(String accessToken) {
        try {
            RestClient restClient = RestClient.create();

            ResponseEntity<NaverProfileDto> response = restClient.get()
                    .uri("https://openapi.naver.com/v1/nid/me")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .toEntity(NaverProfileDto.class);
            
            return response.getBody();
        } catch (Exception e) {
            throw new IllegalStateException("네이버 사용자 정보를 가져오는 중 오류가 발생했습니다.", e);
        }
    }
}