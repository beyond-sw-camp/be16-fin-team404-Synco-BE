package com.team404.synco.common.registry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

// SseEmitterRegistry.java (수정)
@Slf4j
@Component
public class SseEmitterRegistry {
    // 사용자별 다중 연결 보관
    private final Map<String, CopyOnWriteArrayList<SseEmitter>> emittersByUser = new ConcurrentHashMap<>();

    public void registerEmitter(String userId, SseEmitter emitter) {
        emittersByUser.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);
    }

    public void removeEmitter(String userId) {
        emittersByUser.remove(userId);
    }

    public List<SseEmitter> getEmitters(String userId) {
        return emittersByUser.getOrDefault(userId, new CopyOnWriteArrayList<>());
    }

    // 하트비트 등 전체 순회용
    public Map<String, List<SseEmitter>> getAllEmitters() {
        return Collections.unmodifiableMap(emittersByUser);
    }
}