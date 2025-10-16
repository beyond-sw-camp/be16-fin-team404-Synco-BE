package com.team404.synco.common.config;

import com.team404.synco.alarm.service.SseNotificationSubscriber;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @Bean
    @Qualifier("rtInventory")
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(0);
        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    @Qualifier("rtInventory")
    public RedisTemplate<String, String> redisTemplate(@Qualifier("rtInventory") RedisConnectionFactory redisConnectionFactory) {          // 템플릿 객체
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    // member 정보 관련 redis 설정
    @Bean
    @Qualifier("memberInventory")
    public RedisConnectionFactory memberConnectionFactory(){
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(1);
        return new LettuceConnectionFactory(configuration);
    }

    // workSpace 정보 관련 redisTemplate 생성
    @Bean
    @Qualifier("memberInventory")
    public RedisTemplate<String, Object>membereRedisTemplate(
            @Qualifier("memberInventory") RedisConnectionFactory memberConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setConnectionFactory(memberConnectionFactory);
        return redisTemplate;
    }

    // WorkSpace 관련 redis 설정
    @Bean
    @Qualifier("workSpaceInventory")
    public RedisConnectionFactory workSpaceConnectionFactory(){
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(2);
        return new LettuceConnectionFactory(configuration);
    }

    // workSpace 관련 redisTemplate 생성
    @Bean
    @Qualifier("workSpaceInventory")
    public RedisTemplate<String, Object> workSpaceRedisTemplate(
            @Qualifier("workSpaceInventory") RedisConnectionFactory workSpaceConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setConnectionFactory(workSpaceConnectionFactory);
        return redisTemplate;
    }

    // ========== SSE 전용 Redis 설정 ==========
    
    /**
     * SSE Pub/Sub 전용 Redis 연결 팩토리
     */
    @Bean
    @Qualifier("ssePubSub")
    public RedisConnectionFactory sseConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(3); // SSE 전용 DB
        return new LettuceConnectionFactory(configuration);
    }

    /**
     * SSE Pub/Sub 전용 Redis 템플릿
     */
    @Bean
    @Qualifier("ssePubSub")
    public RedisTemplate<String, String> sseRedisTemplate(
            @Qualifier("ssePubSub") RedisConnectionFactory sseConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(sseConnectionFactory);
        return redisTemplate;
    }

    /**
     * Redis 메시지 리스너 컨테이너 (SSE 전용)
     */
    @Bean
    @Qualifier("ssePubSub")
    public RedisMessageListenerContainer sseMessageListenerContainer(
            @Qualifier("ssePubSub") RedisConnectionFactory sseConnectionFactory,
            MessageListenerAdapter sseMessageListenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(sseConnectionFactory);
        container.addMessageListener(sseMessageListenerAdapter, new PatternTopic("status-update"));
        return container;
    }

    /**
     * SSE 메시지 리스너 어댑터
     */
    @Bean
    public MessageListenerAdapter sseMessageListenerAdapter(SseNotificationSubscriber sseNotificationSubscriber) {
        return new MessageListenerAdapter(sseNotificationSubscriber, "onMessage");
    }
}
