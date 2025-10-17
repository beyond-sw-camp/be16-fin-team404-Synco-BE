package com.team404.synco.chat.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;

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
            System.out.println("token : " + token);
            System.out.println("secretkey : " + secretKey);
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Base64.getDecoder().decode(secretKey)) // 변경!
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return Long.parseLong(claims.getSubject().toString());

        } catch (SignatureException e) {
            log.error("JWT 서명 검증 실패");
            return null;
        } catch (Exception e) {
            log.error("JWT 파싱 실패", e);
            return null;
        }
    }
}
