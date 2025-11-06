package com.team404.synco.search.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team404.synco.search.dto.kafka.ChatEvent;
import com.team404.synco.search.index.chat.ChatMessageDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatEventConsumer {

    private final ChatIndexService chatIndexService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "chat.message.created", groupId = "search-service-group")
    public void consumeChatCreated(Map<String, Object> data, Acknowledgment acknowledgment) {
        try {
            ChatEvent event = objectMapper.convertValue(data, ChatEvent.class);
            log.info("📥 ChatMessage 생성 이벤트 수신: chatMessageSeq={}", event.getChatMessageSeq());

            ChatMessageDocument document = ChatMessageDocument.builder()
                    .id("chat_" + event.getChatMessageSeq())
                    .chatMessageSeq(event.getChatMessageSeq())
                    .content(event.getChatMessageText())
                    .workspaceSeq(event.getWorkspaceSeq())
                    .channelSeq(event.getChannelSeq())
                    .channelName(event.getChannelName())
                    .memberSeq(event.getMemberSeq())
                    .createdAt(event.getCreatedAt())
                    .build();

            chatIndexService.index(document);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("❌ ChatMessage 생성 처리 실패: error={}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "chat.message.updated", groupId = "search-service-group")
    public void consumeChatUpdated(Map<String, Object> data, Acknowledgment acknowledgment) {
        try {
            ChatEvent event = objectMapper.convertValue(data, ChatEvent.class);
            log.info("📥 ChatMessage 수정 이벤트 수신: chatMessageSeq={}", event.getChatMessageSeq());

            ChatMessageDocument document = ChatMessageDocument.builder()
                    .id("chat_" + event.getChatMessageSeq())
                    .chatMessageSeq(event.getChatMessageSeq())
                    .content(event.getChatMessageText())
                    .workspaceSeq(event.getWorkspaceSeq())
                    .channelSeq(event.getChannelSeq())
                    .channelName(event.getChannelName())
                    .memberSeq(event.getMemberSeq())
                    .createdAt(event.getCreatedAt())
                    .build();

            chatIndexService.update(document);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("❌ ChatMessage 수정 처리 실패: error={}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "chat.message.deleted", groupId = "search-service-group")
    public void consumeChatDeleted(Long chatMessageSeq, Acknowledgment acknowledgment) {
        try {
            log.info("📥 ChatMessage 삭제 이벤트 수신: chatMessageSeq={}", chatMessageSeq);
            chatIndexService.delete(chatMessageSeq);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("❌ ChatMessage 삭제 처리 실패: error={}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "chat.channel.updated", groupId = "search-service-group")
    public void consumeChannelUpdated(Map<String, Object> data, Acknowledgment acknowledgment) {
        try {
            Long channelSeq = ((Number) data.get("channelSeq")).longValue();
            String channelName = (String) data.get("channelName");
            log.info("📥 Channel 수정 이벤트 수신: channelSeq={}, channelName={}", channelSeq, channelName);
            chatIndexService.updateChannelNameForChannel(channelSeq, channelName);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("❌ Channel 수정 처리 실패: error={}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "chat.channel.deleted", groupId = "search-service-group")
    public void consumeChannelDeleted(Map<String, Object> data, Acknowledgment acknowledgment) {
        try {
            Long channelSeq = ((Number) data.get("channelSeq")).longValue();
            log.info("📥 Channel 삭제 이벤트 수신: channelSeq={}", channelSeq);
            chatIndexService.deleteByChannelSeq(channelSeq);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("❌ Channel 삭제 처리 실패: error={}", e.getMessage(), e);
        }
    }
}

