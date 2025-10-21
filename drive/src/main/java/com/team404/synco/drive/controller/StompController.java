package com.team404.synco.drive.controller;

import com.team404.synco.drive.dto.*;
import com.team404.synco.drive.service.ProjectDocumentRedisService;
import com.team404.synco.drive.service.ProjectDriveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

/**
 * STOMP WebSocket 컨트롤러 - TipTap 기반 실시간 문서 편집
 *
 * TipTap 에디터의 변경사항을 실시간으로 동기화하고
 * Redis를 통해 다른 클라이언트들에게 브로드캐스트
 */
@Controller
@Slf4j
public class StompController {

    private final ProjectDocumentRedisService redisService;
    private final ProjectDriveService projectDriveService;

    public StompController(ProjectDocumentRedisService redisService, ProjectDriveService projectDriveService) {
        this.redisService = redisService;
        this.projectDriveService = projectDriveService;
    }

    // ==================== 문서 편집 메시지 처리 ====================

    @MessageMapping("/document/create")
    public void handleLineCreate(EditorMessageDto messageDto) {
        log.debug("📝 라인 생성 - DocumentId: {}, LineId: {}", messageDto.getDocumentId(), messageDto.getLineId());
        projectDriveService.createDocumentLine(messageDto);
        redisService.publishDocumentUpdateToRedis(messageDto);
    }

    @MessageMapping("/document/update")
    public void handleLineUpdate(EditorMessageDto messageDto) {
        log.debug("✏️ 라인 수정 - DocumentId: {}, LineId: {}", messageDto.getDocumentId(), messageDto.getLineId());
        projectDriveService.updateDocumentLine(messageDto);
        redisService.publishDocumentUpdateToRedis(messageDto);
    }

    @MessageMapping("/document/delete")
    public void handleLineDelete(EditorMessageDto messageDto) {
        log.debug("🗑️ 라인 삭제 - DocumentId: {}, LineId: {}", messageDto.getDocumentId(), messageDto.getLineId());
        projectDriveService.deleteDocumentLine(messageDto);
        redisService.publishDocumentUpdateToRedis(messageDto);
    }

    // ==================== 배치 처리 메시지 ====================

    @MessageMapping("/document/batch-create")
    public void handleBatchLineCreate(EditorMessageDto messageDto) {
        int changeCount = messageDto.getChanges() != null ? messageDto.getChanges().size() : 0;
        log.info("📦 배치 생성 - DocumentId: {}, 개수: {}", messageDto.getDocumentId(), changeCount);
        projectDriveService.createDocumentLines(messageDto);
        redisService.publishDocumentUpdateToRedis(messageDto);
    }

    @MessageMapping("/document/batch-update")
    public void handleBatchLineUpdate(EditorMessageDto messageDto) {
        int changeCount = messageDto.getChanges() != null ? messageDto.getChanges().size() : 0;
        log.info("📦 배치 수정 - DocumentId: {}, 개수: {}", messageDto.getDocumentId(), changeCount);
        projectDriveService.updateDocumentLines(messageDto);
        redisService.publishDocumentUpdateToRedis(messageDto);
    }

    @MessageMapping("/document/batch-delete")
    public void handleBatchLineDelete(EditorMessageDto messageDto) {
        int changeCount = messageDto.getChanges() != null ? messageDto.getChanges().size() : 0;
        log.info("📦 배치 삭제 - DocumentId: {}, 개수: {}", messageDto.getDocumentId(), changeCount);
        projectDriveService.deleteDocumentLines(messageDto);
        redisService.publishDocumentUpdateToRedis(messageDto);
    }

    // ==================== 사용자 관리 ====================

    @MessageMapping("/document/{documentId}/join")
    public void handleUserJoin(@DestinationVariable Long documentId, @Payload UserJoinLeaveDto joinDto) {
        log.info("👤 사용자 접속 - DocumentId: {}, UserId: {}", documentId, joinDto.getUserId());
        redisService.publishUserJoinToRedis(documentId, joinDto);
    }

    @MessageMapping("/document/{documentId}/leave")
    public void handleUserLeave(@DestinationVariable Long documentId, @Payload UserJoinLeaveDto leaveDto) {
        log.info("👋 사용자 이탈 - DocumentId: {}, UserId: {}", documentId, leaveDto.getUserId());
        redisService.publishUserLeaveToRedis(documentId, leaveDto);
    }

    // ==================== 라인 락 관리 ====================

    @MessageMapping("/document/lock")
    public void handleLineLock(EditorMessageDto lockDto) {
        log.info("🔒 라인 잠금 - DocumentId: {}, LineId: {}, UserId: {}", 
            lockDto.getDocumentId(), lockDto.getLineId(), lockDto.getUserId());
        redisService.publishLineLockToRedis(lockDto);
    }

    @MessageMapping("/document/unlock")
    public void handleLineUnlock(EditorMessageDto unlockDto) {
        log.info("🔓 라인 잠금 해제 - DocumentId: {}, LineId: {}, UserId: {}", 
            unlockDto.getDocumentId(), unlockDto.getLineId(), unlockDto.getUserId());
        redisService.publishLineUnlockToRedis(unlockDto);
    }

    // ==================== 커서 관리 ====================

    @MessageMapping("/document/{documentId}/cursor")
    public void handleCursorUpdate(@DestinationVariable Long documentId, @Payload EditorMessageDto cursorDto) {
        log.debug("🖱️ 커서 위치 업데이트 - DocumentId: {}, UserId: {}, LineId: {}", 
            documentId, cursorDto.getUserId(), cursorDto.getLineId());
        redisService.publishCursorUpdateToRedis(documentId, cursorDto);
    }
}
