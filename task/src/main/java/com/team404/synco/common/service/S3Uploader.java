package com.team404.synco.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3Uploader {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public byte[] download(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            return s3Client.getObjectAsBytes(request).asByteArray();
        } catch (Exception e) {
            throw new IllegalArgumentException("S3 다운로드 실패", e);
        }
    }

    /**
     * 스트리밍 방식으로 파일 다운로드 (메모리 효율적)
     */
    public Resource downloadAsStream(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request);
            return new InputStreamResource(response) {
                @Override
                public long contentLength() {
                    return response.response().contentLength();
                }
            };
        } catch (Exception e) {
            throw new IllegalArgumentException("S3 스트리밍 다운로드 실패", e);
        }
    }

    private String extractKeyFromUrl(String fileUrl) {
        int index = fileUrl.indexOf(".amazonaws.com/");
        if (index == -1) throw new IllegalArgumentException("잘못된 S3 URL 형식");
        return URLDecoder.decode(fileUrl.substring(index + ".amazonaws.com/".length()), StandardCharsets.UTF_8);
    }
}


