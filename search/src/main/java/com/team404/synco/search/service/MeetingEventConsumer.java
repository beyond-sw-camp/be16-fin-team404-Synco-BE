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
            log.info("새 MeetingSummary 생성 이벤트 수신: recordingSummarySeq={}", event.getRecordingSummarySeq());

            MeetingSummaryDocument document = MeetingSummaryDocument.builder()
                    .id("meeting_" + event.getRecordingSummarySeq())
                    .recordingSummarySeq(event.getRecordingSummarySeq())
                    .recordingSeq(event.getRecordingSeq())
                    .title(event.getTitle())
                    .description(event.getDescription())
                    .content(event.getSummary())
                    .workspaceSeq(event.getWorkspaceSeq())
                    .roomSeq(event.getRoomSeq())
                    .hostId(event.getHostId())
                    .startedAt(event.getStartedAt())
                    .createdAt(event.getCreatedAt())
                    .build();

            meetingIndexService.index(document);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("MeetingSummary 생성 처리 실패: error={}", e.getMessage(), e);
        }
    }
}

