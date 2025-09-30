package com.team404.synco.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ChatChannelCreateReqDto {
    private String chatChannelName;
    private Long workSpaceSeq;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
