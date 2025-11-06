package com.team404.synco.chat.service;

import com.team404.synco.chat.dto.kafka.ChatEvent;
import com.team404.synco.chat.entity.ChatChannel;
import com.team404.synco.chat.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishMessageCreated(ChatMessage message, ChatChannel channel, Long senderSeq) {
        try {
            ChatEvent event = ChatEvent.builder()
                    .chatMessageSeq(message.getChatMessageSeq())
                    .chatMessageText(message.getChatMessageText())
                    .workspaceSeq(channel.getWorkSpaceSeq())
                    .channelSeq(channel.getChatChannelSeq())
                    .channelName(channel.getChatChannelName())
                    .memberSeq(senderSeq)
                    .createdAt(message.getCreatedAt())
                    .build();
            kafkaTemplate.send("chat.message.created", event);
        } catch (Exception e) {
            log.error("❌ Kafka publish failed: chat.message.created, channelSeq={}, chatMessageSeq={}", channel.getChatChannelSeq(), message.getChatMessageSeq(), e);
        }
    }

    public void publishMessageDeleted(Long chatMessageSeq) {
        try {
            kafkaTemplate.send("chat.message.deleted", chatMessageSeq);
        } catch (Exception e) {
            log.error("❌ Kafka publish failed: chat.message.deleted, chatMessageSeq={}", chatMessageSeq, e);
        }
    }

    public void publishChannelUpdated(ChatChannel channel) {
        try {
            Map<String, Object> event = Map.of(
                    "channelSeq", channel.getChatChannelSeq(),
                    "channelName", channel.getChatChannelName(),
                    "workspaceSeq", channel.getWorkSpaceSeq(),
                    "updatedAt", LocalDateTime.now()
            );
            kafkaTemplate.send("chat.channel.updated", event);
        } catch (Exception e) {
            log.error("❌ Kafka publish failed: chat.channel.updated, channelSeq={}", channel.getChatChannelSeq(), e);
        }
    }

    public void publishChannelDeleted(Long channelSeq, Long workspaceSeq) {
        try {
            Map<String, Object> event = Map.of(
                    "channelSeq", channelSeq,
                    "workspaceSeq", workspaceSeq,
                    "deletedAt", LocalDateTime.now()
            );
            kafkaTemplate.send("chat.channel.deleted", event);
        } catch (Exception e) {
            log.error("❌ Kafka publish failed: chat.channel.deleted, channelSeq={}", channelSeq, e);
        }
    }
}


