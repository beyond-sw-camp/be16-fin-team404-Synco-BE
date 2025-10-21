package com.team404.synco.virtualmeeting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessResponse {
    private String meetingId;
    private String text;                    // STT 결과 텍스트
    private List<Segments> segments;        // STT 세그먼트 정보
    private Map<String, String> saved;      // 저장된 파일 정보
}
