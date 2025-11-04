package com.team404.synco.search.dto.response;

import com.team404.synco.search.index.chat.ChatMessageDocument;
import com.team404.synco.search.index.drive.DriveDocument;
import com.team404.synco.search.index.meeting.MeetingSummaryDocument;
import com.team404.synco.search.index.task.TaskDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultResDto {
    
    private String id;  // "task_123", "drive_456", "chat_789" 등
    
    private String type;  // "task", "file", "message", "meeting"
    
    private String title;  // 검색 결과 제목
    
    private String subtitle;  // 부제목 (보드명, 폴더명, 채널명 등)
    
    private String channelId;  // 선택적 (메시지일 때)
    
    public static SearchResultResDto fromTaskDocument(TaskDocument doc) {
        return SearchResultResDto.builder()
                .id("task_" + doc.getTaskSeq())
                .type("task")
                .title(doc.getTitle())
                .subtitle(doc.getContent())
                .build();
    }
    
    public static SearchResultResDto fromDriveDocument(DriveDocument doc) {
        return SearchResultResDto.builder()
                .id("drive_" + doc.getDocumentSeq())
                .type("file")
                .title(doc.getTitle())
                .subtitle(doc.getFolderName())
                .build();
    }
    
    public static SearchResultResDto fromChatMessageDocument(ChatMessageDocument doc) {
        return SearchResultResDto.builder()
                .id("chat_" + doc.getChatMessageSeq())
                .type("message")
                .title(doc.getContent())
                .subtitle("채널: " + doc.getChannelName())
                .channelId("chat_" + doc.getChannelSeq())
                .build();
    }
    
    public static SearchResultResDto fromMeetingSummaryDocument(MeetingSummaryDocument doc) {
        return SearchResultResDto.builder()
                .id("meeting_" + doc.getRecordingSummarySeq())
                .type("meeting")
                .title(doc.getTitle())
                .subtitle("회의 요약")
                .build();
    }
}

