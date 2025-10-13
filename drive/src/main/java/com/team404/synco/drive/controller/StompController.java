package com.team404.synco.drive.controller;

import com.team404.synco.drive.dto.*;
import com.team404.synco.drive.service.ProjectDocumentRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

/**
 * STOMP WebSocket 컨트롤러 - YJS 기반 실시간 문서 편집
 * 
 * YJS(Yjs)는 CRDT 기반으로 충돌을 자동 해결하므로
 * 백엔드는 메시지 중계 역할만 수행
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class StompController {

    private final ProjectDocumentRedisService redisService;

    // ==================== YJS 핵심 기능 ====================
    
    /**
     * YJS 업데이트 처리 (가장 중요!)
     * YJS가 생성한 바이너리 업데이트를 다른 클라이언트들에게 브로드캐스트
     * + 문서 변경 플래그 설정 (5분 후 DB 동기화)
     */
    @MessageMapping("/document/{documentId}/yjs-update")
    public void handleYjsUpdate(@DestinationVariable Long documentId, @Payload YjsUpdateDto updateDto) {
        log.debug("YJS 업데이트 수신 - DocumentId: {}", documentId);
        
        // YJS 바이너리 업데이트 브로드캐스트
        redisService.publishYjsUpdateToRedis(documentId, updateDto.getUpdate());
        
        // 텍스트 버전이 함께 전송된 경우 Redis 업데이트 + dirty flag 설정
        if (updateDto.getTextContent() != null) {
            redisService.updateDocumentContent(documentId, updateDto.getTextContent());
        }
    }

    // ==================== 사용자 관리 ====================
    
    /**
     * 사용자 접속 처리
     * Redis에 온라인 사용자 추가 및 다른 사용자들에게 알림
     */
    @MessageMapping("/document/{documentId}/join")
    public void handleUserJoin(@DestinationVariable Long documentId, @Payload UserJoinLeaveDto joinDto) {
        log.info("사용자 접속 - DocumentId: {}, UserId: {}", documentId, joinDto.getUserId());
        redisService.publishUserJoinToRedis(documentId, joinDto);
    }

    /**
     * 사용자 이탈 처리
     * Redis에서 온라인 사용자 제거 및 다른 사용자들에게 알림
     */
    @MessageMapping("/document/{documentId}/leave")
    public void handleUserLeave(@DestinationVariable Long documentId, @Payload UserJoinLeaveDto leaveDto) {
        log.info("사용자 이탈 - DocumentId: {}, UserId: {}", documentId, leaveDto.getUserId());
        redisService.publishUserLeaveToRedis(documentId, leaveDto);
    }

    // ==================== 협업 기능 ====================
    
    /**
     * 사용자 커서 위치 업데이트
     * 다른 사용자들이 현재 사용자의 커서 위치를 볼 수 있도록 브로드캐스트
     */
    @MessageMapping("/document/{documentId}/cursor")
    public void handleCursorUpdate(@DestinationVariable Long documentId, @Payload CursorUpdateDto cursorDto) {
        log.debug("커서 업데이트 - DocumentId: {}, UserId: {}", documentId, cursorDto.getUserId());
        redisService.publishCursorUpdateToRedis(documentId, cursorDto);
    }

    // ==================== 동기화 ====================
    
    /**
     * 문서 초기 로드 시 현재 상태 요청
     * - 온라인 사용자 목록
     * - 커서 위치들
     * - YJS 상태 (옵션)
     */
    @MessageMapping("/document/{documentId}/sync")
    public void handleDocumentSync(@DestinationVariable Long documentId) {
        log.info("문서 동기화 요청 - DocumentId: {}", documentId);
        redisService.handleDocumentSync(documentId);
    }
}
