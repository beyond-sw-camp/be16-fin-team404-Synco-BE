package com.team404.synco.apigateway;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.List;

@Component
@Slf4j
public class JwtAuthFilter implements GlobalFilter {

    @Value("${jwt.secretKey}")
    private String secretKey;

    private static final List<String> ALLOWED_PATHS = List.of(
            "/member/create",
            "/member/doLogin",
            "/member/refreshAt"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        log.info("token 검증 시작");
        String bearerToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String path = exchange.getRequest().getURI().getRawPath();
        log.info(path);

        if (ALLOWED_PATHS.contains(path)) {
            return chain.filter(exchange);
        }

        try {
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                throw new IllegalArgumentException("token 관련 예외 발생");
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
        } catch (IllegalArgumentException | MalformedJwtException | ExpiredJwtException | SignatureException |
                 UnsupportedJwtException e) {
            e.printStackTrace();
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }
}
