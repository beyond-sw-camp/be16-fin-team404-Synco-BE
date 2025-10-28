package com.team404.synco.common.registry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SseEmitterRegistry {
    // 동시성 이슈를 줄이기 위한 ConcurrentHashMap 사용
    Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    public void registerEmitter(String userId, SseEmitter emitter) {
        // 연결이 완료(종료)되었을 때
        emitter.onCompletion(() -> {
            emitterMap.remove(userId);
            log.info("[SSE] 연결 종료: {}", userId);
        });

        // 타임아웃 발생 시
        emitter.onTimeout(() -> {
            emitterMap.remove(userId);
            log.info("[SSE] 타임아웃: {}", userId);
            emitter.complete();
        });

        // 에러 발생 시
        emitter.onError((e) -> {
            emitterMap.remove(userId);
            log.info("[SSE] 에러 발생: {}, {}", userId, e.getMessage());
            emitter.completeWithError(e);
        });
        emitterMap.put(userId, emitter);
    }

    public void removeEmitter(String userId) {
        emitterMap.remove(userId);
    }

    public SseEmitter getEmitter(String userId) {
        return emitterMap.get(userId);
    }
}
