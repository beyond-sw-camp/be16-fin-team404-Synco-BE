package com.team404.synco.virtualmeeting.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;



import java.time.Duration;

@Configuration
public class WebClientConfig {
    
    @Value("${asr.api.base-url}")
    private String asrApiBaseUrl;
    
    @Bean
    public WebClient asrWebClient() {
        ConnectionProvider connectionProvider = ConnectionProvider.builder("asr-connection-pool")
                .maxConnections(20)
                .maxIdleTime(Duration.ofSeconds(30))
                .maxLifeTime(Duration.ofMinutes(5))
                .pendingAcquireTimeout(Duration.ofSeconds(10))
                .evictInBackground(Duration.ofSeconds(120))
                .build();
        
        HttpClient httpClient = HttpClient.create(connectionProvider);
        
        return WebClient.builder()
                .baseUrl(asrApiBaseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1)) // 제한 해제
                .build();
    }
}
