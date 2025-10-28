package com.team404.synco.common.config;

import com.team404.synco.chat.service.RedisPubSubService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    /* --------------------------------------------------------
       ✅ 1. Pub/Sub 용 (채팅 메시지 실시간 전송용) - DB 0
    -------------------------------------------------------- */
//    연결기본객체
    @Bean
    @Qualifier("chatPubSub")
    public RedisConnectionFactory chatPubSubFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        return new LettuceConnectionFactory(configuration);
    }

//    publish 객체 (Redis에 데이터를 String 형태로 publish/send하는 객체.)
    @Bean
    @Qualifier("chatPubSub")
    public StringRedisTemplate stringRedisTemplate(@Qualifier("chatPubSub") RedisConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }

//    subscribe 객체
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            @Qualifier("chatPubSub") RedisConnectionFactory redisConnectionFactory,
            MessageListenerAdapter messageListenerAdapter
    ){
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(messageListenerAdapter, new PatternTopic("chat*"));
        return container;
    }

//    redis에서 수신된 메시지를 처리하는 객체 생성
    @Bean
    public MessageListenerAdapter messageListenerAdapter(RedisPubSubService redisPubSubService) {
//        RedisPubSubService의 특정 메서드(onMessage())가 수신된 메시지를 처리할 수 있도록 지정
        return new MessageListenerAdapter(redisPubSubService, "onMessage");
    }

    /* --------------------------------------------------------
      ✅ 2. Member 정보 조회용 (workspace와 공유) - DB 1
    -------------------------------------------------------- */
    @Bean
    @Qualifier("memberInventory")
    public RedisConnectionFactory memberConnectionFactory() {
        RedisStandaloneConfiguration conf = new RedisStandaloneConfiguration();
        conf.setHostName(host);
        conf.setPort(port);
        conf.setDatabase(1); // workspace와 동일하게 DB 1 사용
        return new LettuceConnectionFactory(conf);
    }

    @Bean
    @Qualifier("memberInventory")
    public RedisTemplate<String, Object> memberRedisTemplate(
            @Qualifier("memberInventory") RedisConnectionFactory memberConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(memberConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    // ✅ 3. 기본 RedisTemplate (내부용 Bean)
    @Bean
    public RedisTemplate<String, Object> redisTemplate(@Qualifier("memberInventory") RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }
}
