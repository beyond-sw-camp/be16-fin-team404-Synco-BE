package com.team404.synco.common.service;

import com.team404.synco.common.constant.dto.AlarmResDto;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisEventPublisher {
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisEventPublisher(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void publish(String channel, AlarmResDto alarmResDto) {
        redisTemplate.convertAndSend(channel, alarmResDto);
    }
}
