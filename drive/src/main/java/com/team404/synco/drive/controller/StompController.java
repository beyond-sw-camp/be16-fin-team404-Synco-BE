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

    @MessageMapping("/document/update")
    public void handleLineUpdate(UpdateDocumentReqDto updateDocumentReqDto) {
        if (updateDocumentReqDto.getMethod().equals(DocumentMessageMethod.CREATE_BLOCK)) { // 생성블록
            projectDriveService.createBlock(updateDocumentReqDto);
        } else if (updateDocumentReqDto.getMethod().equals(DocumentMessageMethod.UPDATE_BLOCK)) { // 수정블록
            projectDriveService.updateBlock(updateDocumentReqDto);
        } else if (updateDocumentReqDto.getMethod().equals(DocumentMessageMethod.UPDATE_INDENT_BLOCK)
                || updateDocumentReqDto.getMethod().equals(DocumentMessageMethod.HOT_UPDATE_CONTENTS_BLOCK)) { // 수정블록
            projectDriveService.patchBlockDetails(updateDocumentReqDto);
        } else if (updateDocumentReqDto.getMethod().equals(DocumentMessageMethod.CHANGE_ORDER_BLOCK)) { //순서 변경 블록
            projectDriveService.changeOrderBlock(updateDocumentReqDto);
        } else if (updateDocumentReqDto.getMethod().equals(DocumentMessageMethod.DELETE_BLOCK)) { // 삭제블록
            projectDriveService.deleteBlock(updateDocumentReqDto);
        } else {
            log.error("잘못된 block method");
        }
        redisService.publishDocumentUpdateToRedis(updateDocumentReqDto.getDocumentId(), updateDocumentReqDto);
    }

    @MessageMapping("/document/{documentId}/join")
    public void handleUserJoin(@DestinationVariable Long documentId, @Payload UserJoinLeaveDto joinDto) {
        redisService.publishUserJoinToRedis(documentId, joinDto);
    }

    @MessageMapping("/document/{documentId}/leave")
    public void handleUserLeave(@DestinationVariable Long documentId, @Payload UserJoinLeaveDto leaveDto) {
        redisService.publishUserLeaveToRedis(documentId, leaveDto);
    }

    // TODO: 커서 위치 업데이트 처리
//    @MessageMapping("/document/{documentId}/cursor")
//    public void handleCursorUpdate(@DestinationVariable Long documentId, @Payload CursorPositionDto cursorDto) {
//        redisService.publishCursorUpdateToRedis(documentId, cursorDto);
//    }
}
