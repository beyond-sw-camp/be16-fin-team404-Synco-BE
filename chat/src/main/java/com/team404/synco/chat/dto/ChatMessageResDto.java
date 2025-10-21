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
@Builder
public class ChatMessageResDto {
    private Long chatMessageSeq; // ✅ 메시지 고유 ID 추가
    private Long channelSeq;
    private Long senderSeq; // ✅ Redis key로 조회용
    private String senderName; // ✅ 발신자 이름 추가
    private String senderProfileImageUrl; // ✅ 발신자 프로필 이미지 추가
    private MessageType messageType; // TEXT, FILE, REPLY 등
    private String chatMessageText; // 메시지 내용
    private Long replyToSeq; // 답장 대상 메시지 ID (nullable)
    private String chatMessageFileUrls; // 다중 파일 업로드 (optional)
}
