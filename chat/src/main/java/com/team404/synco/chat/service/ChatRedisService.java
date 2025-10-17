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

    /**
     * Redis에서 멤버 이름 조회
     */
    public String getMemberName(Long memberSeq) {
        String key = MEMBER_KEY_PREFIX + memberSeq;  // 예: memberSeq:5
        Object value = memberRedisTemplate.opsForHash().get(key, "memberName"); // HGET memberSeq:5 memberName
        return value != null ? value.toString() : "Unknown"; // 없으면 기본값 반환
    }

    /**
     * Redis에서 멤버 프로필 이미지 URL 조회
     */
    public String getMemberProfileUrl(Long memberSeq) {
        String key = MEMBER_KEY_PREFIX + memberSeq;
        Object value = memberRedisTemplate.opsForHash().get(key, "memberProfileUrl"); // HGET memberSeq:5 memberProfileUrl
        return value != null ? value.toString() : null; // 없으면 null (프론트에서 기본 이미지 처리)
    }
}
