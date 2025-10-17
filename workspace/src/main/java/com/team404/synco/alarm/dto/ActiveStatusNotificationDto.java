package com.team404.synco.alarm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// ActiveStatus 변경 알림 전용 DTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveStatusNotificationDto {
    
    private Long memberSeq;        // 사용자 고유 식별자 (동명이인 구분용)
    private String memberName;     // 사용자 이름 (표시용)
    private String previousStatus;
    private String currentStatus;
    private LocalDateTime timestamp;
    
    public static ActiveStatusNotificationDto create(Long memberSeq, String memberName, String previousStatus, String currentStatus) {
        return ActiveStatusNotificationDto.builder()
                .memberSeq(memberSeq)
                .memberName(memberName)
                .previousStatus(previousStatus)
                .currentStatus(currentStatus)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
