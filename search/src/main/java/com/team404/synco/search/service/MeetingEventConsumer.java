package com.team404.synco.search.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team404.synco.search.dto.kafka.MeetingEvent;
import com.team404.synco.search.index.meeting.MeetingSummaryDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingEventConsumer {

    private final MeetingIndexService meetingIndexService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "meeting.summary.created", groupId = "search-service-group")
    public void consumeMeetingSummaryCreated(Map<String, Object> data, Acknowledgment acknowledgment) {
        try {
            MeetingEvent event = objectMapper.convertValue(data, MeetingEvent.class);
            log.info("📥 MeetingSummary 생성 이벤트 수신: recordingSummarySeq={}", event.getRecordingSummarySeq());

            // content = summary + transcript
            String content = combineContent(event.getSummary(), event.getTranscript());

            MeetingSummaryDocument document = MeetingSummaryDocument.builder()
                    .id("meeting_" + event.getRecordingSummarySeq())
                    .recordingSummarySeq(event.getRecordingSummarySeq())
                    .recordingSeq(event.getRecordingSeq())
                    .title(event.getTitle())
                    .description(event.getDescription())
                    .content(content)
                    .workspaceSeq(event.getWorkspaceSeq())
                    .roomSeq(event.getRoomSeq())
                    .hostId(event.getHostId())
                    .startedAt(event.getStartedAt())
                    .createdAt(event.getCreatedAt())
                    .build();

            meetingIndexService.index(document);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("❌ MeetingSummary 생성 처리 실패: error={}", e.getMessage(), e);
        }
    }

    /**
     * summary와 transcript를 결합하여 content 생성
     */
    private String combineContent(String summary, String transcript) {
        if (summary == null && transcript == null) {
            return null;
        }
        if (summary == null) {
            return transcript;
        }
        if (transcript == null) {
            return summary;
        }
        return summary + " " + transcript;
    }
}

