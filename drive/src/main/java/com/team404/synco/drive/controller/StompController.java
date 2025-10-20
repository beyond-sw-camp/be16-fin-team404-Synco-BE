package com.team404.synco.drive.controller;

import com.team404.synco.drive.dto.*;
import com.team404.synco.drive.entity.DocumentMessageMethod;
import com.team404.synco.drive.service.ProjectDocumentRedisService;
import com.team404.synco.drive.service.ProjectDriveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.util.Base64;

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

    @MessageMapping("/document/create")
    public void handleLineCreate(EditorMessageDto editorMessageDto) {
        projectDriveService.createDocumentLine(editorMessageDto);
        redisService.publishDocumentUpdateToRedis(editorMessageDto);
    }

    @MessageMapping("/document/update")
    public void handleLineUpdate(EditorMessageDto editorMessageDto) {
        projectDriveService.updateDocumentLine(editorMessageDto);
        redisService.publishDocumentUpdateToRedis(editorMessageDto);
    }

    @MessageMapping("/document/delete")
    public void handleLineDelete(EditorMessageDto editorMessageDto) {
        projectDriveService.deleteDocumentLine(editorMessageDto);
        redisService.publishDocumentUpdateToRedis(editorMessageDto);
    }

    @MessageMapping("/document/batch-create")
    public void handleBatchLineCreate(EditorMessageDto editorMessageDto) {
        log.info("📦 배치 생성 요청 - DocumentId: {}, 개수: {}", 
            editorMessageDto.getDocumentId(), 
            editorMessageDto.getChanges() != null ? editorMessageDto.getChanges().size() : 0);
        projectDriveService.createDocumentLines(editorMessageDto);
        redisService.publishDocumentUpdateToRedis(editorMessageDto);
    }

    @MessageMapping("/document/batch-update")
    public void handleBatchLineUpdate(EditorMessageDto editorMessageDto) {
        log.info("📦 배치 수정 요청 - DocumentId: {}, 개수: {}", 
            editorMessageDto.getDocumentId(), 
            editorMessageDto.getChanges() != null ? editorMessageDto.getChanges().size() : 0);
        projectDriveService.updateDocumentLines(editorMessageDto);
        redisService.publishDocumentUpdateToRedis(editorMessageDto);
    }

    @MessageMapping("/document/batch-delete")
    public void handleBatchLineDelete(EditorMessageDto editorMessageDto) {
        log.info("📦 배치 삭제 요청 - DocumentId: {}, 개수: {}", 
            editorMessageDto.getDocumentId(), 
            editorMessageDto.getChanges() != null ? editorMessageDto.getChanges().size() : 0);
        projectDriveService.deleteDocumentLines(editorMessageDto);
        redisService.publishDocumentUpdateToRedis(editorMessageDto);
    }

    @MessageMapping("/document/{documentId}/join")
    public void handleUserJoin(@DestinationVariable Long documentId, @Payload UserJoinLeaveDto joinDto) {
        redisService.publishUserJoinToRedis(documentId, joinDto);
    }

    @MessageMapping("/document/{documentId}/leave")
    public void handleUserLeave(@DestinationVariable Long documentId, @Payload UserJoinLeaveDto leaveDto) {
        redisService.publishUserLeaveToRedis(documentId, leaveDto);
    }

    @MessageMapping("/document/lock")
    public void handleLineLock(EditorMessageDto lockDto) {
        log.info("🔒 라인 잠금 요청 - DocumentId: {}, LineId: {}, UserId: {}", 
            lockDto.getDocumentId(), lockDto.getLineId(), lockDto.getUserId());
        redisService.publishLineLockToRedis(lockDto);
    }

    @MessageMapping("/document/unlock")
    public void handleLineUnlock(EditorMessageDto unlockDto) {
        log.info("🔓 라인 잠금 해제 요청 - DocumentId: {}, LineId: {}, UserId: {}", 
            unlockDto.getDocumentId(), unlockDto.getLineId(), unlockDto.getUserId());
        redisService.publishLineUnlockToRedis(unlockDto);
    }

    // TODO: 커서 위치 업데이트 처리
//    @MessageMapping("/document/{documentId}/cursor")
//    public void handleCursorUpdate(@DestinationVariable Long documentId, @Payload CursorPositionDto cursorDto) {
//        redisService.publishCursorUpdateToRedis(documentId, cursorDto);
//    }
}
