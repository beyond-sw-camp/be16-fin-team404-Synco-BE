package com.team404.synco.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SseEmitterRegistry {
    
    // Thread-safe한 ConcurrentHashMap 사용
    private final Map<Long, SseEmitter> emitterMap = new ConcurrentHashMap<>();
    

    // SSE 연결 등록
    public void addSseEmitter(Long memberSeq, SseEmitter sseEmitter) {
        // 기존 연결이 있다면 완료 처리 후 새 연결로 교체
        SseEmitter existingEmitter = emitterMap.get(memberSeq);
        if (existingEmitter != null) {
            existingEmitter.complete();
        }
        
        emitterMap.put(memberSeq, sseEmitter);
        
        // 연결 완료 시 자동 제거
        sseEmitter.onCompletion(() -> removeSseEmitter(memberSeq));
        sseEmitter.onTimeout(() -> removeSseEmitter(memberSeq));
        sseEmitter.onError(throwable -> removeSseEmitter(memberSeq));
    }
    

    // SSE 연결 제거
    public void removeSseEmitter(Long memberSeq) {
        emitterMap.remove(memberSeq);
    }
    
    // 특정 사용자의 SSE 연결 조회
    public SseEmitter getEmitter(Long memberSeq) {
        return emitterMap.get(memberSeq);
    }
    
    // 현재 연결된 사용자 수 조회
    public int getConnectedUserCount() {
        return emitterMap.size();
    }

    // 모든 연결된 사용자 ID 조회
    public java.util.Set<Long> getConnectedMembers() {
        return emitterMap.keySet();
    }
}
