package com.team404.synco.search.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team404.synco.search.dto.kafka.DriveEvent;
import com.team404.synco.search.index.drive.DriveDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriveEventConsumer {

    private final DriveIndexService driveIndexService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "drive.document.created", groupId = "search-service-group")
    public void consumeDriveCreated(Map<String, Object> data, Acknowledgment acknowledgment) {
        try {
            DriveEvent event = objectMapper.convertValue(data, DriveEvent.class);
            log.info("📥 Drive 생성 이벤트 수신: documentSeq={}", event.getDocumentSeq());

            DriveDocument document = DriveDocument.builder()
                    .id("drive_" + event.getDocumentSeq())
                    .documentSeq(event.getDocumentSeq())
                    .title(event.getDocumentName())
                    .documentType(event.getDocumentType())
                    .documentUrl(event.getDocumentUrl())
                    .fileSize(event.getFileSize())
                    .workspaceSeq(event.getWorkspaceSeq())
                    .folderName(event.getFolderName())
                    .folderSeq(event.getFolderSeq())
                    .memberSeq(event.getMemberSeq())
                    .createdAt(event.getCreatedAt())
                    .build();

            driveIndexService.index(document);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("❌ Drive 생성 처리 실패: error={}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "drive.document.updated", groupId = "search-service-group")
    public void consumeDriveUpdated(Map<String, Object> data, Acknowledgment acknowledgment) {
        try {
            DriveEvent event = objectMapper.convertValue(data, DriveEvent.class);
            log.info("📥 Drive 수정 이벤트 수신: documentSeq={}", event.getDocumentSeq());

            DriveDocument document = DriveDocument.builder()
                    .id("drive_" + event.getDocumentSeq())
                    .documentSeq(event.getDocumentSeq())
                    .title(event.getDocumentName())
                    .documentType(event.getDocumentType())
                    .documentUrl(event.getDocumentUrl())
                    .fileSize(event.getFileSize())
                    .workspaceSeq(event.getWorkspaceSeq())
                    .folderName(event.getFolderName())
                    .folderSeq(event.getFolderSeq())
                    .memberSeq(event.getMemberSeq())
                    .createdAt(event.getCreatedAt())
                    .build();

            driveIndexService.update(document);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("❌ Drive 수정 처리 실패: error={}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "drive.document.deleted", groupId = "search-service-group")
    public void consumeDriveDeleted(Long documentSeq, Acknowledgment acknowledgment) {
        log.info("📥 Drive 삭제 이벤트 수신: documentSeq={}", documentSeq);

        try {
            driveIndexService.delete(documentSeq);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("❌ Drive 삭제 처리 실패: documentSeq={}, error={}", documentSeq, e.getMessage(), e);
        }
    }
}

