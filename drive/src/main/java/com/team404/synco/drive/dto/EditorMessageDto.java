package com.team404.synco.drive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditorMessageDto {
    public enum MessageType {
        CREATE, // 새 라인 생성
        UPDATE, // 라인 수정
        DELETE, // 라인 삭제
        BATCH_CREATE, // 여러 라인 생성
        BATCH_UPDATE, // 여러 라인 수정
        BATCH_DELETE, // 여러 라인 삭제
        CURSOR_UPDATE, // 커서 위치 업데이트
        USER_JOIN,   // 사용자 입장
        USER_LEAVE,  // 사용자 퇴장
        LOCK,   // 라인 잠금
        UNLOCK  // 라인 잠금 해제
    }

    private MessageType messageType;

    private String documentId; // 어떤 문서에 대한 메시지인지 식별
    private String senderId;   // 누가 보냈는지 식별 (임시 ID 또는 사용자 ID)
    
    // 단일 라인 처리용 필드
    private String lineId;
    private String prevLineId;
    private String content;
    
    // 배치 처리용 필드
    private List<LineChange> changes;
    
    // 락 관련 추가 필드
    private Long userId;       // 실제 사용자 ID (락 관리용)
    private String userName;   // 사용자 이름 (UI 표시용)
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LineChange {
        private String lineId;
        private String prevLineId;
        private String content;
    }
}
