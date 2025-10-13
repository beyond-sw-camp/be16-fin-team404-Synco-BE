package com.team404.synco.drive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 타이핑 상태 DTO
 * "사용자 A가 입력 중..." 표시용
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypingStatusDto {
    /**
     * 타이핑 중인 사용자 ID
     */
    private Long userId;
    
    /**
     * 사용자 이름
     */
    private String userName;
    
    /**
     * 타이핑 상태 (true: 입력 중, false: 입력 중지)
     */
    private Boolean isTyping;
}

