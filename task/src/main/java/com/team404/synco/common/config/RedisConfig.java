package com.team404.synco.common.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
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

    // ✅ 3. 기본 RedisTemplate (내부용 Bean)
    @Bean
    public RedisTemplate<String, Object> redisTemplate(@Qualifier("memberInventory") RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }
}