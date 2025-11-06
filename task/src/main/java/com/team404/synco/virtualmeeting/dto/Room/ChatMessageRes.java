package com.team404.synco.virtualmeeting.dto.Room;

import com.team404.synco.virtualmeeting.entity.Message;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessageRes {
    private Long id;
    private Long senderId;
    private String name;
    private String content;
    private LocalDateTime createdAt;

    public static ChatMessageRes fromEntity(Message message) {
        return ChatMessageRes.builder()
                .id(message.getId())
                .senderId(message.getSenderId())
                .content(message.getContent())
                .name(message.getId().toString()) // 임시로
                .createdAt(message.getCreatedAt())
                .build();
    }
}
