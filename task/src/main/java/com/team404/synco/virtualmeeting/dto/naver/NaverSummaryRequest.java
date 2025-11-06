package com.team404.synco.virtualmeeting.dto.naver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NaverSummaryRequest {
    
    private List<String> texts;              // 요약 대상 문장 (1~35,000자)
    
    @Builder.Default
    private Boolean autoSentenceSplitter = true;  // 문단 분리 허용 여부
    
    @Builder.Default
    private Integer segCount = -1;           // 문단 분리 수 (-1: 자동)
    
    @Builder.Default
    private Integer segMaxSize = 1000;       // 한 문단의 최대 글자 수
    
    @Builder.Default
    private Integer segMinSize = 300;        // 한 문단의 최소 글자 수
    
    @Builder.Default
    private Boolean includeAiFilters = false; // AI 필터 적용 여부
}
