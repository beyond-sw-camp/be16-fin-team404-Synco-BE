package com.team404.synco.virtualmeeting.config;

import io.livekit.server.EgressServiceClient;
import io.livekit.server.IngressServiceClient;
import io.livekit.server.RoomServiceClient;
import io.livekit.server.WebhookReceiver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LiveKitConfig {

    @Value("${livekit.api.host}")
    private String LIVEKIT_API_HOST;

    @Value("${livekit.api.key}")
    private String LIVEKIT_API_KEY;

    @Value("${livekit.api.secret}")
    private String LIVEKIT_API_SECRET;

    public LiveKitConfig(
            @Value("${livekit.api.host}") String LIVEKIT_API_HOST,
            @Value("${livekit.api.key}") String LIVEKIT_API_KEY,
            @Value("${livekit.api.secret}") String LIVEKIT_API_SECRET){
        this.LIVEKIT_API_HOST = LIVEKIT_API_HOST;
        this.LIVEKIT_API_KEY = LIVEKIT_API_KEY;
        this.LIVEKIT_API_SECRET = LIVEKIT_API_SECRET;
    }

    @Bean
    public RoomServiceClient roomServiceClient() {
        return RoomServiceClient.createClient(LIVEKIT_API_HOST, LIVEKIT_API_KEY, LIVEKIT_API_SECRET);
    }

    @Bean
    public EgressServiceClient egressServiceClient() {
        return EgressServiceClient.createClient(LIVEKIT_API_HOST, LIVEKIT_API_KEY, LIVEKIT_API_SECRET);
    }

    @Bean
    public IngressServiceClient ingressServiceClient() {
        return IngressServiceClient.createClient(LIVEKIT_API_HOST, LIVEKIT_API_KEY, LIVEKIT_API_SECRET);
    }

    @Bean
    public WebhookReceiver webhookReceiver() {
        return new WebhookReceiver(LIVEKIT_API_KEY, LIVEKIT_API_SECRET);
    }
}
