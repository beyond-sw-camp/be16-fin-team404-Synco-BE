package com.team404.synco.virtualmeeting.config;

import io.livekit.server.EgressServiceClient;
import io.livekit.server.RoomServiceClient;
import io.livekit.server.WebhookReceiver;
import livekit.LivekitEgress;
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

    @Value("${cloud.aws.credentials.access-key}")
    private String AWS_ACCESS_KEY;

    @Value("${cloud.aws.credentials.secret-key}")
    private String AWS_SECRET_KEY;

    @Value("${cloud.aws.region.static}")
    private String AWS_REGION;

    @Value("${cloud.aws.s3.bucket}")
    private String AWS_S3_BUCKET;

    @Bean
    public RoomServiceClient roomServiceClient() {
        return RoomServiceClient.createClient(LIVEKIT_API_HOST, LIVEKIT_API_KEY, LIVEKIT_API_SECRET);
    }

    @Bean
    public EgressServiceClient egressServiceClient() {
        return EgressServiceClient.createClient(LIVEKIT_API_HOST, LIVEKIT_API_KEY, LIVEKIT_API_SECRET);
    }

    @Bean
    public LivekitEgress.S3Upload s3Upload() {
        return LivekitEgress.S3Upload.newBuilder()
                .setAccessKey(AWS_ACCESS_KEY)
                .setSecret(AWS_SECRET_KEY)
                .setRegion(AWS_REGION)
                .setBucket(AWS_S3_BUCKET)
                .build();
    }

    @Bean
    public WebhookReceiver webhookReceiver() {
        return new WebhookReceiver(LIVEKIT_API_KEY, LIVEKIT_API_SECRET);
    }
}
