package com.team404.synco.alarm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team404.synco.alarm.dto.AlarmResDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlarmSubscriber {
    private final AlarmService alarmService;
    private final ObjectMapper objectMapper;

    public void onMessage(String message, String channel) throws IOException {
        log.info("[Redis] 구독 수신 → {}", message);
        AlarmResDto alarmResDto = objectMapper.readValue(message, AlarmResDto.class);
        alarmService.createAlarm(alarmResDto);
    }
}
