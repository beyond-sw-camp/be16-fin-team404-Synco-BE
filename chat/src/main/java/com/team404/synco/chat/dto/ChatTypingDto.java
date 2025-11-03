package com.team404.synco.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatTypingDto {
    private Long channelSeq;
    private Long senderSeq;
    private String senderName;
    private boolean typing; // ✅ true: 타이핑 중, false: 중지
}
