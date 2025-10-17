package com.team404.synco.chat.dto;

import com.team404.synco.chat.entity.MessageType;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChatMessageReqDto {
    private Long channelSeq;
    private Long senderSeq;               // ✅ Redis key로 조회용
    private MessageType messageType;      // TEXT, FILE, REPLY 등
    private String chatMessageText;       // 메시지 내용
    private Long replyToSeq;              // 답장 대상 메시지 ID (nullable)
    private List<MultipartFile> files;    // 다중 파일 업로드 (optional)
}
