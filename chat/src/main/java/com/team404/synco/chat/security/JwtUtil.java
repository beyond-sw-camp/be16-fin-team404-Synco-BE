package com.team404.synco.chat.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secretKey}")
    private String secretKey;

    public Long extractMemberSeq(String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Claims claims = Jwts
                    .parserBuilder()
                    .setSigningKey(secretKey.getBytes()) // ✅ String → byte[]
                    .build()                             // ✅ build() 필수
                    .parseClaimsJws(token)
                    .getBody();

            return Long.parseLong(claims.get("memberSeq").toString());

        } catch (SignatureException e) {
            log.error("JWT 서명 검증 실패");
            return null;
        } catch (Exception e) {
            log.error("JWT 파싱 실패", e);
            return null;
        }
    }
}
