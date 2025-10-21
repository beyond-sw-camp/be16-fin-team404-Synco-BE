package com.team404.synco.drive.config;

import com.team404.synco.drive.service.ProjectDocumentRedisService;
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
import org.springframework.data.redis.serializer.RedisSerializer;
@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Bean
    @Qualifier("documentFactory")
    public RedisConnectionFactory documentFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(redisHost);
        configuration.setPort(redisPort);
        configuration.setDatabase(12); // 문서 편집용 DB 12번
        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    @Qualifier("documentRedisTemplate")
    public RedisTemplate<String, String> redisTemplate(
            @Qualifier("documentFactory") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    // Pub/Sub: 서버 간 메시지 브로드캐스트
    @Bean
    @Qualifier("documentPubSub")
    public RedisConnectionFactory documentPubSubConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(redisHost);
        configuration.setPort(redisPort);
        // Redis pub/sub에서는 특정 데이터베이스에 의존적이지 않음
        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    @Qualifier("documentPubSubTemplate")
    public RedisTemplate<String, String> documentPubSubTemplate(
            @Qualifier("documentPubSub") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        return template;
    }

    // subscribe 객체
    @Bean
    @Qualifier("documentMessageListenerContainer")
    public RedisMessageListenerContainer documentMessageListenerContainer(
            @Qualifier("documentPubSub") RedisConnectionFactory redisConnectionFactory,
            MessageListenerAdapter messageListenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);

        // 문서 편집 관련 패턴들 구독
        container.addMessageListener(messageListenerAdapter, new PatternTopic("/topic/document/*/document-update"));
        container.addMessageListener(messageListenerAdapter, new PatternTopic("/topic/document/*/online-users"));
        container.addMessageListener(messageListenerAdapter, new PatternTopic("/topic/document/*/line-locks"));
        container.addMessageListener(messageListenerAdapter, new PatternTopic("/topic/document/*/cursor-update"));
        return container;
    }

    @Bean
    public MessageListenerAdapter messageListenerAdapter(ProjectDocumentRedisService redisService) {
        return new MessageListenerAdapter(redisService, "onMessage");
    }

    // ================================
    // Redis Keyspace Notifications (키 만료 이벤트 감지)
    // ================================
    @Bean
    @Qualifier("keyExpirationListenerContainer")
    public RedisMessageListenerContainer keyExpirationListenerContainer(
            @Qualifier("documentFactory") RedisConnectionFactory redisConnectionFactory,
            MessageListenerAdapter messageListenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        
        // DB 12번의 expired 이벤트 구독 - ProjectDocumentRedisService의 onMessage()로 통합!
        container.addMessageListener(messageListenerAdapter, new PatternTopic("__keyevent@12__:expired"));
        
        return container;
    }

    // ================================
    // 온라인 사용자 관리 (Hash 구조: userId -> userName)
    // ================================
    @Bean
    @Qualifier("documentOnlineUsers")
    public RedisTemplate<String, String> documentOnlineUsersRedisTemplate(
            @Qualifier("documentFactory") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    // ================================
    // 워크스페이스 멤버 조회용 (DB 2: Workspace Service와 공유)
    // ================================
    @Bean
    @Qualifier("workspaceMembers")
    public RedisConnectionFactory workspaceMembersConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(redisHost);
        configuration.setPort(redisPort);
        configuration.setDatabase(2); // 워크스페이스 서비스 DB
        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    @Qualifier("workspaceMembers")
    public RedisTemplate<String, Object> workspaceMembersRedisTemplate(
            @Qualifier("workspaceMembers") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
