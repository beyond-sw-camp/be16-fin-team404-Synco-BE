package com.team404.synco.alarm.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team404.synco.alarm.dto.ActiveStatusNotificationDto;
import com.team404.synco.common.constant.ActiveStatus;
import com.team404.synco.common.service.SseEmitterRegistry;
import com.team404.synco.member.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SseNotificationService {
    
    private final SseEmitterRegistry sseEmitterRegistry;
    private final SseNotificationPublisher sseNotificationPublisher;
    private final ObjectMapper objectMapper;
    
    // ActiveStatus 변경 알림을 친구들에게 발송
    public void notifyStatusChangeToFriends(Member member, ActiveStatus previousStatus, ActiveStatus newStatus, List<Member> friendList) {
        ActiveStatusNotificationDto message = ActiveStatusNotificationDto.create(
                member.getName(),
                previousStatus.toString(),
                newStatus.toString()
        );
        
        String messageJson = serializeActiveStatusMessage(message);
        
        // 친구들에게 개별적으로 알림 발송
        for (Member friend : friendList) {
            notifySingleFriend(friend.getMemberSeq(), messageJson);
        }
        
    }
    
    // 특정 친구에게 상태 변경 알림 발송 (로컬 또는 Redis Pub/Sub)
    private void notifySingleFriend(Long friendMemberSeq, String messageJson) {
        SseEmitter sseEmitter = sseEmitterRegistry.getEmitter(friendMemberSeq);
        
        if (sseEmitter != null) {
            // 현재 서버에 연결된 친구에게 직접 전송
            sendDirectNotification(sseEmitter, messageJson);
        } else {
            // 다른 서버 인스턴스에 있는 친구에게 Redis Pub/Sub으로 전달
            sseNotificationPublisher.publish(friendMemberSeq, messageJson);
        }
    }
    
    // SSE로 직접 ActiveStatus 변경 알림 전송
    private void sendDirectNotification(SseEmitter sseEmitter, String messageJson) {
        try {
            sseEmitter.send(SseEmitter.event()
                    .name("STATUS_CHANGE")
                    .data(messageJson));
        } catch (IOException e) {
            throw new RuntimeException("SSE 직접 전송 실패", e);
        }
    }
    
    // ActiveStatus 변경 메시지 직렬화
    private String serializeActiveStatusMessage(ActiveStatusNotificationDto message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("ActiveStatus 메시지 직렬화 실패", e);
        }
    }
}
