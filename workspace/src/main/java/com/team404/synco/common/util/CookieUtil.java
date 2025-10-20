package com.team404.synco.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CookieUtil {
    
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    
    @Value("${cookie.secure}")
    private boolean secure;
    
    @Value("${cookie.max-age}")
    private int maxAge;
    
    @Value("${cookie.same-site}")
    private String sameSite;

    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)                      // JavaScript 접근 차단
                .secure(secure)                      // 환경별 설정 (local: false, prod: true)
                .path("/")                           // 모든 경로에서 전송
                .maxAge(Duration.ofSeconds(maxAge))  // 유효기간
                .sameSite(sameSite)                  // CSRF 방어 (환경별 설정)
                .build();
    }

    public ResponseCookie deleteRefreshTokenCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(0)                           // 즉시 만료
                .sameSite(sameSite)
                .build();
    }
}

