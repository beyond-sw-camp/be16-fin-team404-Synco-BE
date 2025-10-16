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
    
    private String memberName;
    private String previousStatus;
    private String currentStatus;
    private LocalDateTime timestamp;
    
    public static ActiveStatusNotificationDto create(String memberName, String previousStatus, String currentStatus) {
        return ActiveStatusNotificationDto.builder()
                .memberName(memberName)
                .previousStatus(previousStatus)
                .currentStatus(currentStatus)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
