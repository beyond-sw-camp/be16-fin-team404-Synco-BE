package com.team404.synco.common.auth;


import com.team404.synco.member.entity.Member;
import com.team404.synco.member.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.expirationAt}")
    private int expirationAt;

    @Value("${jwt.secretKeyAt}")
    private String secretKeyAt;

    @Value("${jwt.expirationRt}")
    private int expirationRt;

    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;

    private Key secretAtKey;
    private Key secretRtKey;

    private final MemberRepository memberRepository;

    private final RedisTemplate<String, String> redisTemplate;

    public JwtTokenProvider(MemberRepository memberRepository, @Qualifier("rtInventory") RedisTemplate<String, String> redisTemplate) {
        this.memberRepository = memberRepository;
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        secretAtKey = new SecretKeySpec(java.util.Base64.getDecoder().decode(secretKeyAt)
                , SignatureAlgorithm.HS512.getJcaName());

        secretRtKey = new SecretKeySpec(java.util.Base64.getDecoder().decode(secretKeyRt)
                , SignatureAlgorithm.HS512.getJcaName());
    }

    public String createAtToken(Member member) {
        Long memberId = member.getMemberSeq();
        init();

        Claims claims = Jwts.claims().setSubject(String.valueOf(memberId));

        Date now = new Date();
        String token =Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationAt * 90 * 1000L))
                .signWith(secretAtKey)
                .compact();

        return token;
    }

    public String createRtToken(Member member) {
        Long memberId = member.getMemberSeq();
        init();

        Claims claims = Jwts.claims().setSubject(String.valueOf(memberId));

        Date now = new Date();
        String refreshToken =Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationRt * 90 * 1000L))
                .signWith(secretRtKey)
                .compact();

        redisTemplate.opsForValue().set(String.valueOf(memberId), refreshToken);
        return refreshToken;
    }

    public Member validateRt(String refreshToken) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKeyRt)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();

        Long memberId = Long.parseLong(claims.getSubject());
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        String redisRt = redisTemplate.opsForValue().get(member.getEmail());
        if (!redisRt.equals(refreshToken)) {
            throw new IllegalArgumentException("잘못된 토큰 입니다.");
        }

        return member;
    }
}
