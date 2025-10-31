package com.team404.synco.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

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

    private String extractKeyFromUrl(String fileUrl) {
        int index = fileUrl.indexOf(".amazonaws.com/");
        if (index == -1) throw new IllegalArgumentException("잘못된 S3 URL 형식");
        return URLDecoder.decode(fileUrl.substring(index + ".amazonaws.com/".length()), StandardCharsets.UTF_8);
    }
}


