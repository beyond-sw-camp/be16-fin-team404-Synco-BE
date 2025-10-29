package com.team404.synco.common.config;

import com.team404.synco.alarm.service.AlarmSubscriber;
import com.team404.synco.common.constant.AlarmType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

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

    // redis-pub/sub용 redis 설정
    @Bean
    @Qualifier("sseFactory")
    public RedisConnectionFactory sseFactory(){
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        return new LettuceConnectionFactory(configuration);
    }

    // redis-pub/sub용 redisTemplate 생성
    @Bean
    @Qualifier("ssePubSub")
    public RedisTemplate<String, Object> sseRedisTemplate(@Qualifier("sseFactory") RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    @Bean
    public MessageListenerAdapter listenerAdapter(AlarmSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "onMessage");
    }

    @Bean
    @Qualifier("listenerContainer")
    public RedisMessageListenerContainer redisContainer(
            @Qualifier("sseFactory") RedisConnectionFactory factory,
            MessageListenerAdapter listenerAdapter) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);

        // AlarmType에 정의된 모든 채널을 수동으로 등록
        List<String> alarmChannels = List.of(
                AlarmType.MEETING,
                AlarmType.PROJECT,
                AlarmType.CHAT,
                AlarmType.TASK,
                AlarmType.FRIEND,
                AlarmType.DRIVE
        );

        alarmChannels.forEach(channel ->
                container.addMessageListener(listenerAdapter, new ChannelTopic(channel))
        );

        return container;
    }
}
