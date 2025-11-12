package com.team404.synco.virtualmeeting.config;

import io.livekit.server.EgressServiceClient;
import io.livekit.server.RoomServiceClient;
import io.livekit.server.WebhookReceiver;
import livekit.LivekitEgress;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

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
        // Dispatcher 설정: 동시 요청 수 증가
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(64);              // 전체 최대 동시 요청 수
        dispatcher.setMaxRequestsPerHost(10);       // 호스트당 최대 동시 요청 수
        
        // ConnectionPool 설정: 연결 재사용
        ConnectionPool connectionPool = new ConnectionPool(
                10,                    // 최대 idle 연결 수
                5, TimeUnit.MINUTES    // 연결 유지 시간
        );
        
        // 타임아웃 설정을 늘린 OkHttpClient 생성
        OkHttpClient customHttpClient = new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .connectionPool(connectionPool)
                .connectTimeout(30, TimeUnit.SECONDS)  // 연결 타임아웃: 30초
                .readTimeout(60, TimeUnit.SECONDS)     // 읽기 타임아웃: 60초
                .writeTimeout(30, TimeUnit.SECONDS)    // 쓰기 타임아웃: 30초
                .build();
        
        // Supplier<OkHttpClient>로 래핑하여 전달
        return EgressServiceClient.createClient(LIVEKIT_API_HOST, LIVEKIT_API_KEY, LIVEKIT_API_SECRET, () -> customHttpClient);
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
