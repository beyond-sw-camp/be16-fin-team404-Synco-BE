package com.team404.synco.search.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchReqDto {
    
    private String query;  // 검색 키워드
    
    private List<String> types;  // ["task", "file", "message", "meeting"] - 선택적 (없으면 전체)
    
    @Builder.Default
    private Integer page = 0;  // 기본 0
    
    @Builder.Default
    private Integer size = 20;  // 기본 20
}

