package com.team404.synco.virtualmeeting.dto.naver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NaverSummaryResponse {
    
    private Status status;
    
    private Result result;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Status {
        private String code;
        private String message;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Result {
        private String text;           // 요약 결과
        private Integer inputTokens;   // 입력 토큰 수
    }
}
