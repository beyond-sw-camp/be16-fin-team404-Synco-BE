package com.team404.synco.search.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team404.synco.search.dto.kafka.TaskEvent;
import com.team404.synco.search.index.task.TaskDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskEventConsumer {

    private final TaskIndexService taskIndexService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "task.task.created", groupId = "search-service-group")
    public void consumeTaskCreated(Map<String, Object> data, Acknowledgment acknowledgment) {
        try {
            TaskEvent event = objectMapper.convertValue(data, TaskEvent.class);
            log.info("📥 Task 생성 이벤트 수신: taskSeq={}", event.getTaskSeq());

            TaskDocument document = TaskDocument.builder()
                    .id("task_" + event.getTaskSeq())
                    .taskSeq(event.getTaskSeq())
                    .title(event.getTaskTitle())
                    .content(event.getTaskContent())
                    .taskStatus(event.getTaskStatus())
                    .startDate(event.getStartDate())
                    .endDate(event.getEndDate())
                    .workspaceSeq(event.getWorkspaceSeq())
                    .memberSeq(event.getMemberSeq())
                    .createdAt(event.getCreatedAt())
                    .build();

            taskIndexService.index(document);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("❌ Task 생성 처리 실패: error={}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "task.task.updated", groupId = "search-service-group")
    public void consumeTaskUpdated(Map<String, Object> data, Acknowledgment acknowledgment) {
        try {
            TaskEvent event = objectMapper.convertValue(data, TaskEvent.class);
            log.info("📥 Task 수정 이벤트 수신: taskSeq={}", event.getTaskSeq());

            TaskDocument document = TaskDocument.builder()
                    .id("task_" + event.getTaskSeq())
                    .taskSeq(event.getTaskSeq())
                    .title(event.getTaskTitle())
                    .content(event.getTaskContent())
                    .taskStatus(event.getTaskStatus())
                    .startDate(event.getStartDate())
                    .endDate(event.getEndDate())
                    .workspaceSeq(event.getWorkspaceSeq())
                    .memberSeq(event.getMemberSeq())
                    .createdAt(event.getCreatedAt())
                    .build();

            taskIndexService.update(document);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("❌ Task 수정 처리 실패: error={}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "task.task.deleted", groupId = "search-service-group")
    public void consumeTaskDeleted(Long taskSeq, Acknowledgment acknowledgment) {
        log.info("📥 Task 삭제 이벤트 수신: taskSeq={}", taskSeq);

        try {
            taskIndexService.delete(taskSeq);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("❌ Task 삭제 처리 실패: taskSeq={}, error={}", taskSeq, e.getMessage(), e);
        }
    }
}

