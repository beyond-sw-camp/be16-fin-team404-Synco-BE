package com.team404.synco.common.service;

import com.team404.synco.common.constant.dto.AlarmResDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisEventPublisher {
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisEventPublisher(@Qualifier("ssePubSub") RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void publish(String channel, AlarmResDto alarmResDto) {
        redisTemplate.convertAndSend(channel, alarmResDto);
    }
}
