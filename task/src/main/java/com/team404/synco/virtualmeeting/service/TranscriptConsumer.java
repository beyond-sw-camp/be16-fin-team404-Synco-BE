package com.team404.synco.virtualmeeting.service;

import com.team404.synco.virtualmeeting.dto.kafka.TranscriptEvent;
import com.team404.synco.virtualmeeting.entity.Recording;
import com.team404.synco.virtualmeeting.entity.RecordingSummary;
import com.team404.synco.virtualmeeting.repository.RecordingRepository;
import com.team404.synco.virtualmeeting.repository.RecordingSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TranscriptConsumer {

    private static final String TOPIC_TRANSCRIPT = "transcript-completed";
    private final RecordingRepository recordingRepository;
    private final RecordingSummaryRepository recordingSummaryRepository;
    private final SummaryService summaryService;

    @KafkaListener(topics = TOPIC_TRANSCRIPT, groupId = "task-service-group")
    public void consumeTranscript(TranscriptEvent event, Acknowledgment acknowledgment) {
        log.info("📥 Transcript 수신: recordingSeq={}", event.getRecordingSeq());

        try {
            Recording recording = recordingRepository.findById(event.getRecordingSeq())
                    .orElseThrow(() -> new RuntimeException("Recording을 찾을 수 없습니다: " + event.getRecordingSeq()));

            // RecordingSummary 생성 또는 업데이트
            RecordingSummary summary = RecordingSummary.builder()
                    .recording(recording)
                    .transcript(event.getTranscript())
                    .build();

            recordingSummaryRepository.save(summary);
            log.info("✅ Transcript 저장 완료: recordingSeq={}", event.getRecordingSeq());

            // Naver Summary API로 요약 생성
            String summaryText = summaryService.generateSummary(event.getTranscript());
            
            summary.updateSummary(summaryText);
            recordingSummaryRepository.save(summary);
            log.info("✅ 요약 생성 완료: recordingSeq={}", event.getRecordingSeq());

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("❌ Transcript 처리 실패: {}", e.getMessage(), e);
        }
    }
}

