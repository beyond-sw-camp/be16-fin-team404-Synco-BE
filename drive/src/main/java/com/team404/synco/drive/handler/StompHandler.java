//package com.team404.synco.drive.handler;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.MessageChannel;
//import org.springframework.messaging.simp.stomp.StompCommand;
//import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
//import org.springframework.messaging.support.ChannelInterceptor;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class StompHandler implements ChannelInterceptor {
//
//    @Value("${jwt.secretKeyAt}")
//    private String secretKey;
//
////    private final ProjectDocumentRedisService projectDocumentRedisService;
//
//    public Message<?> preSend(Message<?> message, MessageChannel channel) {
//        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
//
//        if(StompCommand.CONNECT == accessor.getCommand()){
////            log.info("WebSocket 연결 요청 - 토큰 검증 시작");
////            String bearerToken = accessor.getFirstNativeHeader("Authorization");
////
////            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
////                throw new RuntimeException("인증 헤더가 없거나 Bearer 토큰이 아닙니다.");
////            }
////
////            String token = bearerToken.substring(7);
////
////            Jwts.parserBuilder()
////                    .setSigningKey(secretKey)
////                    .build()
////                    .parseClaimsJws(token)
////                    .getBody();
////            log.info("토큰 유효성 검증 성공");
//        }
//
//        if(StompCommand.SUBSCRIBE == accessor.getCommand()){
////            log.info("문서 구독 요청 - 권한 검증 시작");
////            String bearerToken = accessor.getFirstNativeHeader("Authorization");
////            String token = bearerToken.substring(7);
////            Claims claims = Jwts.parserBuilder()
////                    .setSigningKey(secretKey)
////                    .build()
////                    .parseClaimsJws(token)
////                    .getBody();
////            Long userId = Long.valueOf(claims.getSubject());
////            Long documentId = Long.valueOf(accessor.getDestination().split("/")[3]);
////            if(!projectDocumentRedisService.isMemberOfWorkspace(userId, documentId)){
////                throw new RuntimeException("해당 문서에 접근 권한이 없습니다.");
////            }
//        }
//        return message;
//    }
//}
