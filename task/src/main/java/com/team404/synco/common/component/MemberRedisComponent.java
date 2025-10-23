package com.team404.synco.common.component;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class MemberRedisComponent {
    private final RedisTemplate<String, Object> memberRedisTemplate;
    private static final String MEMBER_KEY_PREFIX = "memberSeq:";

    public MemberRedisComponent(@Qualifier("memberInventory") RedisTemplate<String, Object> memberRedisTemplate) {
        this.memberRedisTemplate = memberRedisTemplate;
    }

    public String getMemberName(final long memberSeq) {
        return Objects.requireNonNull(memberRedisTemplate.opsForHash().get(MEMBER_KEY_PREFIX + memberSeq, "memberName")).toString();
    }

    public String getMemberProfileUrl(final long memberSeq) {
        return Objects.requireNonNull(memberRedisTemplate.opsForHash().get(MEMBER_KEY_PREFIX + memberSeq, "memberProfileUrl")).toString();
    }
}