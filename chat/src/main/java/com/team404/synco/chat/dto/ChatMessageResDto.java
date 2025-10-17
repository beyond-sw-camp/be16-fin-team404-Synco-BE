package com.team404.synco.chat.dto;

import com.team404.synco.chat.entity.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResDto {
    private Long messageSeq;
    private Long channelSeq;
    private Long senderSeq;
    private String senderName;
    private MessageType messageType;
    private String messageContent;
    private Long replyToSeq;
    private List<String> fileUrls; // ✅ 업로드 완료 후 URL 반환
}
