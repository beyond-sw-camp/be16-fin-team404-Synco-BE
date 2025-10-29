package com.team404.synco.chat.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChatRedisService {

    private final RedisTemplate<String, Object> memberRedisTemplate;

    public ChatRedisService(@Qualifier("memberInventory") RedisTemplate<String, Object> memberRedisTemplate) {
        this.memberRedisTemplate = memberRedisTemplate;
    }

    private static final String MEMBER_KEY_PREFIX = "memberSeq:"; // 키 규칙: memberSeq:{멤버ID}

    // 따옴표 제거
    private String cleanValue(Object value) {
        if (value == null) return null;
        return value.toString().replace("\"", "");
    }

    /**
     * Redis에서 멤버 이름 조회
     */
    public String getMemberName(Long memberSeq) {
        String key = MEMBER_KEY_PREFIX + memberSeq;
        Object value = memberRedisTemplate.opsForHash().get(key, "memberName");
        String name = cleanValue(value);
        return name != null ? name : "알 수 없음";
    }

    /**
     * Redis에서 멤버 프로필 이미지 URL 조회
     */
    public String getMemberProfileUrl(Long memberSeq) {
        String key = MEMBER_KEY_PREFIX + memberSeq;
        Object value = memberRedisTemplate.opsForHash().get(key, "memberProfileUrl");
        return cleanValue(value);
    }
}
