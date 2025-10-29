package com.team404.synco.virtualmeeting.service;

import com.team404.synco.virtualmeeting.dto.naver.NaverSummaryRequest;
import com.team404.synco.virtualmeeting.dto.naver.NaverSummaryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;

@Slf4j
@Service
public class SummaryService {

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String apiKey;

    public SummaryService(
            @Value("${naver.summary.api.url}") String apiUrl,
            @Value("${naver.summary.api.api-key}") String apiKey
    ) {
        this.restTemplate = new RestTemplate();
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
    }

    public String generateSummary(String transcript) {
        if (transcript == null || transcript.trim().isEmpty()) {
            log.warn("요약할 텍스트가 비어있습니다.");
            return "요약할 내용이 없습니다.";
        }

        log.info("📝 요약 생성 시작: 길이={}", transcript.length());

        try {
            // CLOVA Studio Summarization API 요청 형식에 맞춰 요청 생성
            NaverSummaryRequest request = NaverSummaryRequest.builder()
                    .texts(Arrays.asList(transcript))  // List<String> 형식으로 전달
                    .autoSentenceSplitter(true)
                    .segCount(-1)  // 자동 분리
                    .segMaxSize(1000)
                    .segMinSize(300)
                    .includeAiFilters(false)
                    .build();

            // HTTP Headers 설정 (CLOVA Studio 형식)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);  // Bearer 토큰 형식

            // HTTP Entity 생성
            HttpEntity<NaverSummaryRequest> entity = new HttpEntity<>(request, headers);

            // API 호출
            ResponseEntity<NaverSummaryResponse> response = restTemplate.exchange(
                    URI.create(apiUrl),
                    HttpMethod.POST,
                    entity,
                    NaverSummaryResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                NaverSummaryResponse result = response.getBody();
                
                // Status 체크
                if (result.getStatus() != null && !"20000".equals(result.getStatus().getCode())) {
                    log.error("❌ 네이버 Summary API 에러: {} - {}", 
                            result.getStatus().getCode(), result.getStatus().getMessage());
                    return "요약 생성 중 오류가 발생했습니다: " + result.getStatus().getMessage();
                }

                // Result에서 text 추출
                String summary = (result.getResult() != null && result.getResult().getText() != null) 
                        ? result.getResult().getText() 
                        : "요약 생성 완료";
                
                log.info("✅ 요약 생성 완료: 길이={}, inputTokens={}", 
                        summary.length(), 
                        result.getResult() != null ? result.getResult().getInputTokens() : 0);
                return summary;
            } else {
                log.error("❌ 네이버 Summary API 호출 실패: statusCode={}", response.getStatusCode());
                return "요약 생성 중 오류가 발생했습니다.";
            }

        } catch (Exception e) {
            log.error("❌ 요약 생성 실패: {}", e.getMessage(), e);
            return "요약 생성 중 오류가 발생했습니다: " + e.getMessage();
        }
    }
}

