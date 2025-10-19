package com.team404.synco.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3Uploader {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    /**
     * ✅ 단일 파일 업로드 (폴더 자동 생성 + 중복검증)
     * @param file 업로드할 파일
     * @param folderPath 예: chat/15
     * @return 업로드된 파일 URL
     */
    public String upload(MultipartFile file, String folderPath) {
        validateFile(file);
        try {
            // ✅ 고유 파일명 생성
            String uniqueFileName = generateUniqueFileName(file.getOriginalFilename());
            String key = folderPath + "/" + uniqueFileName;

            // 1️⃣ 폴더 없으면 생성
            ensureFolderExists(folderPath);

            // 2️⃣ 중복 파일명 체크
            if (fileExists(key)) {
                log.warn("⚠️ 중복 파일명 존재, 업로드 생략: {}", key);
                return s3Client.utilities().getUrl(b -> b.bucket(bucket).key(key)).toExternalForm();
            }

            // 3️⃣ 실제 업로드
            try (InputStream in = file.getInputStream()) {
                PutObjectRequest request = PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(file.getContentType())
                        .build();

                s3Client.putObject(request, RequestBody.fromInputStream(in, file.getSize()));
            }

            String url = s3Client.utilities().getUrl(b -> b.bucket(bucket).key(key)).toExternalForm();
            log.info("✅ S3 업로드 성공 - {}", url);
            return url;

        } catch (Exception e) {
            throw new IllegalArgumentException("S3 업로드 실패: " + file.getOriginalFilename(), e);
        }
    }

    /** ✅ 폴더 존재 확인 후 없으면 생성 */
    private void ensureFolderExists(String folderPath) {
        try {
            s3Client.headObject(b -> b.bucket(bucket).key(folderPath + "/"));
        } catch (Exception e) {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(folderPath + "/")
                            .build(),
                    RequestBody.empty()
            );
            log.info("📁 폴더 생성됨: {}", folderPath);
        }
    }

    /** ✅ 중복 파일명 체크 */
    private boolean fileExists(String key) {
        try {
            s3Client.headObject(b -> b.bucket(bucket).key(key));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 파일명 검증
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }
        long maxSize = 100 * 1024 * 1024; // 100MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("파일 크기는 100MB를 초과할 수 없습니다.");
        }

        String ext = getExtension(file.getOriginalFilename());
        if (!ext.matches("\\.(jpg|jpeg|svg|png|gif|webp|pdf|mp4|mov|avi|mkv|doc|docx|xls|xlsx|ppt|pptx|txt|zip|rar)$")) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다.");
        }
    }

    /**
     * ✅ 고유 파일명 생성
     * 예: 20251019_ab12cd34_회의록.pdf
     */
    private String generateUniqueFileName(String original) {
        String dateStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomId = UUID.randomUUID().toString().substring(0, 8);
        return dateStamp + "_" + randomId + "_" + original;
    }

    /**
     * 확장자 추출
     */
    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new IllegalArgumentException("파일 확장자가 없습니다.");
        }
        return fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
    }

    // ✅ Presigned URL 생성 (1분 유효)
    public String createPresignedUrl(String key) {
        AwsBasicCredentials creds = AwsBasicCredentials.create(accessKey, secretKey);
        try (S3Presigner presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(creds))
                .build()) {

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(1))
                    .getObjectRequest(getObjectRequest)
                    .build();

            String presignedUrl = presigner.presignGetObject(presignRequest).url().toString();
            log.info("🎫 Presigned URL 생성: {}", presignedUrl);
            return presignedUrl;
        }
    }
}
