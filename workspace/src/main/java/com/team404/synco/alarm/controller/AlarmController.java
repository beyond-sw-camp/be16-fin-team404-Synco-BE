package com.team404.synco.alarm.controller;

import com.team404.synco.common.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequestMapping("/sse")
@RequiredArgsConstructor
public class AlarmController {
    private final SseService sseService;

    @GetMapping("/connect")
    public SseEmitter subscribe(@RequestHeader("X-Member-Seq")Long memberSeq) {
            return sseService.connect(memberSeq);
    }
}
