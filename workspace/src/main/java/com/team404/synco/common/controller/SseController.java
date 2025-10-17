package com.team404.synco.common.controller;

import com.team404.synco.common.service.SseEmitterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
@Slf4j
public class SseController {
    
    private final SseEmitterRegistry sseEmitterRegistry;
    
    private static final long SSE_TIMEOUT = 30 * 60 * 1000L; // 30분
    
    // SSE 연결 생성
    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@RequestHeader("X-Member-Seq") Long memberSeq) {
        SseEmitter sseEmitter = new SseEmitter(SSE_TIMEOUT);
        sseEmitterRegistry.addSseEmitter(memberSeq, sseEmitter);
        
        try {
            // 연결 확인 메시지 전송
            sseEmitter.send(SseEmitter.event()
                    .name("connect")
                    .data("SSE 연결이 성공적으로 설정되었습니다."));
        } catch (Exception e) {
            sseEmitter.completeWithError(e);
        }
        
        return sseEmitter;
    }
    
    // SSE 연결 해제
    @PostMapping("/disconnect")
    public ResponseEntity<String> disconnect(@RequestHeader("X-Member-Seq") Long memberSeq) {
        sseEmitterRegistry.removeSseEmitter(memberSeq);
        
        return ResponseEntity.ok("SSE 연결이 해제되었습니다.");
    }
    
}
