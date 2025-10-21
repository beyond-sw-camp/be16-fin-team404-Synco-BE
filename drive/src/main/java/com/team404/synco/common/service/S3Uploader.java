package com.team404.synco.common.service;

import com.team404.synco.drive.util.ContentTypeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3Uploader {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 파일 업로드 (폴더/랜덤파일명 구조, 항상 고유 보장)
     */
    public String upload(MultipartFile file, String folder) {
        validateFile(file);

        String key = folder + "/" + generateUniqueFileName(file.getOriginalFilename());

        // ✅ ContentTypeUtil을 사용하여 정확한 Content-Type 설정
        String contentType = ContentTypeUtil.getContentType(file.getOriginalFilename());
        
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)  // ✅ ContentTypeUtil에서 가져온 정확한 타입
                .build();

        try (InputStream in = file.getInputStream()) {
            s3Client.putObject(request, RequestBody.fromInputStream(in, file.getSize()));
        } catch (IOException e) {
            throw new IllegalArgumentException("S3 업로드 실패", e);
        }

        String url = s3Client.utilities().getUrl(b -> b.bucket(bucket).key(key)).toExternalForm();
        log.info("S3 업로드 성공 - Key: {}, URL: {}", key, url);
        return url;
    }

    /**
     * 파일 다운로드
     */
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
     * 파일 삭제
     */
    public void delete(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
            log.info("S3 삭제 성공 - Key: {}", key);
        } catch (Exception e) {
            throw new IllegalArgumentException("S3 삭제 실패", e);
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

        // ✅ 모든 확장자 허용 (ContentTypeUtil에서 처리)
        String ext = getExtension(file.getOriginalFilename());
        // 확장자 제한 제거 - 모든 파일 타입 허용
    }

    /**
     * 고유 파일명 생성
     */
    private String generateUniqueFileName(String original) {
        String originalFileName = original;
        String dateStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomId = UUID.randomUUID().toString().substring(0, 8);
        return dateStamp + "_" + randomId + "_" +originalFileName;
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

    /**
     * URL → S3 Key 추출
     */
    private String extractKeyFromUrl(String fileUrl) {
        int index = fileUrl.indexOf(".amazonaws.com/");
        if (index == -1) throw new IllegalArgumentException("잘못된 S3 URL 형식");
        return URLDecoder.decode(fileUrl.substring(index + ".amazonaws.com/".length()), java.nio.charset.StandardCharsets.UTF_8);
    }
}
