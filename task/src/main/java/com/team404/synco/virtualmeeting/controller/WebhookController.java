package com.team404.synco.virtualmeeting.controller;

import com.team404.synco.common.constant.dto.ResponseDto;
import com.team404.synco.virtualmeeting.dto.LiveKitWebhookDto;
import com.team404.synco.virtualmeeting.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/webhooks")
public class WebhookController {

    private final RoomService roomService;

    @PostMapping("/livekit")
    public ResponseEntity<ResponseDto<String>> handleLiveKitWebhook(@RequestBody LiveKitWebhookDto webhookDto) {
        try {
            log.info("LiveKit Webhook 수신: event={}, room={}, participant={}", 
                webhookDto.getEvent(), 
                webhookDto.getRoom() != null ? webhookDto.getRoom().getName() : "null",
                webhookDto.getParticipant() != null ? webhookDto.getParticipant().getIdentity() : "null");

            // 서비스에서 모든 이벤트 처리
            roomService.handleWebhookEvent(webhookDto);

            return ResponseEntity.ok(ResponseDto.ok("Webhook processed successfully", HttpStatus.OK));
            
        } catch (Exception e) {
            log.error("Webhook 처리 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDto.fail(HttpStatus.INTERNAL_SERVER_ERROR, "Webhook processing failed"));
        }
    }

    // Webhook 상태 확인용 헬스체크 엔드포인트
    @GetMapping("/livekit/health")
    public ResponseEntity<ResponseDto<String>> healthCheck() {
        return ResponseEntity.ok(ResponseDto.ok("Webhook endpoint is healthy", HttpStatus.OK));
    }
}
