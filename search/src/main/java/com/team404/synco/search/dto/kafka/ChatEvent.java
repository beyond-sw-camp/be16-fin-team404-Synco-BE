package com.team404.synco.search.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatEvent {
    private Long chatMessageSeq;
    private String chatMessageText;  // content
    private Long workspaceSeq;
    private Long channelSeq;
    private String channelName;
    private Long memberSeq;
    private LocalDateTime createdAt;
}

