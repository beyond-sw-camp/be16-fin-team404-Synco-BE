package com.team404.synco.virtualmeeting.service;

import com.team404.synco.virtualmeeting.dto.kafka.RecordingCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecordingEventPublisher {

    private static final String TOPIC_RECORDING_COMPLETED = "recording-completed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishRecordingCompleted(RecordingCompletedEvent event) {
        try {
            log.info("📤 녹화 완료 이벤트 발행: recordingSeq={}, roomSeq={}, url={}", 
                    event.getRecordingSeq(), event.getRoomSeq(), event.getOutputUrl());
            kafkaTemplate.send(TOPIC_RECORDING_COMPLETED, event.getRecordingSeq().toString(), event);
        } catch (Exception e) {
            log.error("❌ 녹화 완료 이벤트 발행 실패: {}", e.getMessage(), e);
        }
    }
}

