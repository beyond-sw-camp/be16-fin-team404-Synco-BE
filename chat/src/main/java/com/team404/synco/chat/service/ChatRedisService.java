package com.team404.synco.chat.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
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

    /**
     * Redis에서 멤버 활성 상태 조회
     */
    public String getMemberActiveStatus(Long memberSeq) {
        String key = MEMBER_KEY_PREFIX + memberSeq;
        Object value = memberRedisTemplate.opsForHash().get(key, "activeStatus");
        String activeStatus = cleanValue(value);
        return activeStatus != null ? activeStatus : "OFFLINE"; // 기본값: OFFLINE
    }

    /**
     * Redis에서 멤버 워크스페이스 목록 조회
     */
    public List<Long> getMemberWorkSpaceList(Long memberSeq) {
        String key = MEMBER_KEY_PREFIX + memberSeq;
        Object value = memberRedisTemplate.opsForHash().get(key, "workSpaceList");

        if (value == null) {
            return new ArrayList<>();
        }

        try {
            String rawWorkSpaceList = value.toString();
            String cleaned = rawWorkSpaceList.replaceAll("^\"|\"$", "");

            if (cleaned.startsWith("[") && cleaned.endsWith("]")) {
                String[] items = cleaned.substring(1, cleaned.length() - 1).split(",");
                return Arrays.stream(items)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("workSpaceList 파싱 실패: memberSeq={}, raw={}", memberSeq, value);
        }

        return new ArrayList<>();
    }
}
