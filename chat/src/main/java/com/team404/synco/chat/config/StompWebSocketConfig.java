package com.team404.synco.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final StompHandler stompHandler;

    public StompWebSocketConfig(StompHandler stompHandler) {
        this.stompHandler = stompHandler;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/connect")    //클라이언트는 이 URL로 WebSocket 핸드셰이크(최초 연결 요청)를 보냄
                .setAllowedOriginPatterns("http://localhost:3000", "https://www.synco1.shop")
                .withSockJS(); //SockJS라이브러리(ws://가 아닌 http:// 엔드포인트를 사용할 수 있게 해줌)를 통한 요청을 허용하는 설정.
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
//        /publish/1 형태로 메시지 발행해야 함을 설정
//        /publish로 시작하는 url패턴으로 메시지가 발행되면 @Controller객체의 @MessageMapping메서드로 라우팅
        registry.setApplicationDestinationPrefixes("/publish");

//        TODO: kafka로 메시지 수신(STOMP 채팅서버 enableSimpleBroker → Kafka 브로커)
//        /topic/1 형태로 메시지를 수신(subscribe)해야 함을 설정
        registry.enableSimpleBroker("/topic");
    }

//    웹소켓요청(connect, subscribe, disconnect)등이 요청시에는 http header등 http메시지를 넣어올 수 있고, 이를 interceptor를 통해 가로채 토큰등을 검증할 수 있음.
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler);
    }
}
