package com.team404.synco.apigateway;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.List;

@Component
@Slf4j
public class JwtAuthFilter implements GlobalFilter {
 
    @Value("${jwt.secretKey}")
    private String secretKey;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    // TODO: oauth 로그인 url 추가 에정
    private static final List<String> ALLOWED_PATHS = List.of(
            "/member/create",
            "/member/doLogin",
            "/member/refreshAt",
            "/member/findId",
            "/member/findPassword",
            "/member/refreshAt",
            "/member/google/doLogin",
            "/member/kakao/doLogin",
            "/member/naver/doLogin",
            "/connect/**",
            "/livekit/**"
    );

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        log.info("token 검증 시작");
        String bearerToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String path = exchange.getRequest().getURI().getRawPath();
        log.info(path);

        // ✅ 패턴 매칭으로 허용된 경로 확인
        for (String allowedPath : ALLOWED_PATHS) {
            if (pathMatcher.match(allowedPath, path)) {
                log.info("허용된 경로: {}", path);
                return chain.filter(exchange);
            }
        }

        try {
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                throw new SecurityException("Authorization 헤더가 없거나 형식이 잘못되었습니다.");
            }
            String token = bearerToken.substring(7);

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String memberSeq = claims.getSubject();

            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(builder -> builder
                            .header("X-Member-Seq", memberSeq)
                    )
                    .build();

            return chain.filter(modifiedExchange);
        } catch (SecurityException e) {
            log.warn("인증 헤더 오류 ({}): {}", exchange.getRequest().getRemoteAddress(), e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        } catch (JwtException e) {
            log.warn("JWT 토큰 오류 ({}): {}", exchange.getRequest().getRemoteAddress(), e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }
}
