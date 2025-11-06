package com.team404.synco.search.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedSearchResDto {
    
    private List<SearchResultResDto> results;  // 검색 결과 리스트
    
    private Long total;  // 전체 개수
    
    private Map<String, Long> facets;  // 타입별 개수 {"task": 20, "file": 15, ...}
}

