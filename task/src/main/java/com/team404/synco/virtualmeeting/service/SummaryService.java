package com.team404.synco.virtualmeeting.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SummaryService {

    // TODO: Naver Summary API 연동
    public String generateSummary(String transcript) {
        // 임시 구현
        log.info("요약 생성 중: 길이={}", transcript != null ? transcript.length() : 0);
        
        // 실제로는 Naver Summary API를 호출
        // https://api.ncloud-docs.com/docs/ai-naver-clovaspeech-summarization
        
        return "요약 내용이 여기에 들어갑니다. (Naver Summary API 구현 필요)";
    }
}

