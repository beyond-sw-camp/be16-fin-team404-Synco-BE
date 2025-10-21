package com.team404.synco.virtualmeeting.service;

import com.team404.synco.virtualmeeting.dto.ProcessResponse;
import com.team404.synco.virtualmeeting.dto.Segments;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsrService {
    
    private final WebClient asrWebClient;

    /**
     * 오디오 파일을 ASR 서비스로 전송하여 처리
     */
    public ProcessResponse processAudio(byte[] audioData, String fileName, String meetingId) {
        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", new ByteArrayResource(audioData) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            }, MediaType.APPLICATION_OCTET_STREAM);

            if (meetingId != null) {
                builder.part("meetingId", meetingId);
            }

            ProcessResponse response = asrWebClient.post()
                    .uri("/v1/process")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(ProcessResponse.class)
                    .block();

            log.info("ASR 처리 완료: meetingId={}, segments={}",
                    response.getMeetingId(),
                    response.getSegments() != null ? response.getSegments().size() : 0);

            return response;

        } catch (WebClientResponseException e) {
            log.error("ASR 서비스 HTTP 오류: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("ASR 서비스 HTTP 오류: " + e.getStatusCode() + " - " + e.getMessage(), e);
        } catch (WebClientRequestException e) {
            log.error("ASR 서비스 연결 오류 (타임아웃 또는 네트워크): {}", e.getMessage());
            throw new RuntimeException("ASR 서비스 연결 실패: " + e.getMessage() + " (FastAPI 서버가 실행 중인지 확인하세요)", e);
        } catch (Exception e) {
            log.error("ASR 처리 중 예상치 못한 오류 발생", e);
            throw new RuntimeException("ASR 처리 중 오류 발생: " + e.getMessage(), e);
        }
    }

    /**
     * ProcessResponse를 Segments 리스트로 변환
     */
    public List<Segments> convertToSegments(ProcessResponse response) {
        return response.getSegments();
    }
}
