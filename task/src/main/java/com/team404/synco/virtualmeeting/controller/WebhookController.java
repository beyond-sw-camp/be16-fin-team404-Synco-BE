package com.team404.synco.virtualmeeting.controller;

import com.team404.synco.common.constant.dto.ResponseDto;
import com.team404.synco.virtualmeeting.service.LiveKitService;
import io.livekit.server.WebhookReceiver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import livekit.LivekitWebhook.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/webhooks")
public class WebhookController {

    private final LiveKitService liveKitService;
    private final WebhookReceiver webhookReceiver;


    @PostMapping(value = "/livekit", consumes = "application/webhook+json")
    public ResponseEntity<ResponseDto<String>> handleLiveKitWebhook(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody String body
    ) {
        try {
            WebhookEvent event = webhookReceiver.receive(body,authHeader);
            liveKitService.handleWebhook(event);

            return ResponseEntity.ok(ResponseDto.ok("Webhook processed successfully", HttpStatus.OK));
            
        } catch (Exception e) {
            log.error("Webhook 처리 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDto.fail(HttpStatus.INTERNAL_SERVER_ERROR, "Webhook processing failed"));
        }
    }
}
