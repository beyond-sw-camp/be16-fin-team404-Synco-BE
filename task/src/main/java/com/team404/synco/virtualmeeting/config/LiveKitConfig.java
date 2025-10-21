package com.team404.synco.virtualmeeting.config;

import io.livekit.server.RoomServiceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LiveKitConfig {

    @Bean
    public RoomServiceClient roomServiceClient(
            @Value("${livekit.url}") String url,
            @Value("${livekit.apiKey}") String apiKey,
            @Value("${livekit.apiSecret}") String apiSecret) {

        return RoomServiceClient.createClient(url, apiKey, apiSecret);
    }
}
