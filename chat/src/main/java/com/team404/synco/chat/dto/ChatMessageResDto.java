package com.team404.synco.chat.dto;

import com.team404.synco.chat.entity.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResDto {
//    private Long messageSeq;
//    private String senderName;
    private Long channelSeq;
    private Long senderSeq;          // ✅ Redis key로 조회용
    private MessageType messageType;      // TEXT, FILE, REPLY 등
    private String chatMessageText;       // 메시지 내용
    private Long replyToSeq;              // 답장 대상 메시지 ID (nullable)
    private List<String> files;    // 다중 파일 업로드 (optional)
}
