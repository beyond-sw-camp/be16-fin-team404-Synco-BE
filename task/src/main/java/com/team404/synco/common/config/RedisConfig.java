package com.team404.synco.common.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

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

    // WorkSpace 관련 redis 설정 (워크스페이스와 동일)
    @Bean
    @Qualifier("workSpaceInventory")
    public RedisConnectionFactory workSpaceConnectionFactory(){
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(2);
        return new LettuceConnectionFactory(configuration);
    }

    // workSpace 관련 redisTemplate 생성 (워크스페이스와 동일)
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

    // ✅ 4. 기본 RedisTemplate (내부용 Bean)
    @Bean
    public RedisTemplate<String, Object> redisTemplate(@Qualifier("memberInventory") RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }

    // redis-pub/sub용 redis 설정
    @Bean
    @Qualifier("ssePubSub")
    public RedisConnectionFactory sseFactory(){

        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        // redis pub/sub 기능은 db에 값을 저장하는 기능이 아니므로, 특정 db에 의존적이지 않음
        return new LettuceConnectionFactory(configuration);
    }

    // redis-pub/sub용 redisTemplate 생성
    @Bean
    @Qualifier("ssePubSub")
    public RedisTemplate<String, String> sseRedisTemplate(@Qualifier("ssePubSub") RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }
}