package com.team404.synco.member.service;

import com.team404.synco.member.dto.AccessTokenDto;
import com.team404.synco.member.dto.GoogleProfileDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class GoogleService {

    @Value("${oauth.google.client-id}")
    private String googleClientId;

    @Value("${oauth.google.client-secret}")
    private String googleClientSecret;

    @Value("${oauth.google.redirect-uri}")
    private String googleRedirectUri;


    public AccessTokenDto getAccessToken(String code){
        try {
            RestClient restClient = RestClient.create();

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("code", code);
            params.add("client_id", googleClientId);
            params.add("client_secret", googleClientSecret);
            params.add("redirect_uri", googleRedirectUri);
            params.add("grant_type", "authorization_code");

            ResponseEntity<AccessTokenDto> response =  restClient.post()
                    .uri("https://oauth2.googleapis.com/token")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .body(params)
                    .retrieve()
                    .toEntity(AccessTokenDto.class);

            return response.getBody();
        } catch (Exception e) {
            throw new IllegalStateException("구글 로그인 중 오류가 발생했습니다.", e);
        }
    }

    public GoogleProfileDto getGoogleProfile(String token){
        try {
            RestClient restClient = RestClient.create();
            ResponseEntity<GoogleProfileDto> response =  restClient.get()
                    .uri("https://openidconnect.googleapis.com/v1/userinfo")
                    .header("Authorization", "Bearer "+token)
                    .retrieve()
                    .toEntity(GoogleProfileDto.class);
            
            return response.getBody();
        } catch (Exception e) {
            throw new IllegalStateException("구글 사용자 정보를 가져오는 중 오류가 발생했습니다.", e);
        }
    }
}