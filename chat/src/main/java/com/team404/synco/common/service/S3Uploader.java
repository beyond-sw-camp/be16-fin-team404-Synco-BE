package com.team404.synco.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3Uploader {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "pdf", "mp4", "mov", "avi", "mkv",
            "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "zip", "rar"
    );

    /** ✅ 단일 파일 업로드 */
    public String upload(MultipartFile file, Long channelSeq) {
        validateFile(file);
        String folderPath = "chat/" + channelSeq;

        try (InputStream in = file.getInputStream()) {
            String uniqueFileName = generateUniqueFileName(file.getOriginalFilename());
            String key = folderPath + "/" + uniqueFileName;

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(in, file.getSize()));
            String url = s3Client.utilities().getUrl(b -> b.bucket(bucket).key(key)).toExternalForm();

            log.info("✅ S3 업로드 성공 - {}", url);
            return url;
        } catch (Exception e) {
            throw new IllegalArgumentException("S3 업로드 실패: " + file.getOriginalFilename(), e);
        }
    }

    /** ✅ 다중 파일 업로드 */
    public List<String> uploadAll(List<MultipartFile> files, Long channelSeq) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }
        if (files.size() > 20) {
            throw new IllegalArgumentException("최대 20개의 파일만 업로드할 수 있습니다.");
        }

        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            urls.add(upload(file, channelSeq));
        }
        return urls;
    }

    /** ✅ 파일명 검증 */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        long maxSize = 100 * 1024 * 1024; // 100MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("파일 크기는 100MB를 초과할 수 없습니다.");
        }

        String ext = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다: ." + ext);
        }
    }

    /** ✅ 고유 파일명 생성 (예: 20251020_ab12cd34_회의록.pdf) */
    private String generateUniqueFileName(String original) {
        String dateStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomId = UUID.randomUUID().toString().substring(0, 8);
        return dateStamp + "_" + randomId + "_" + original;
    }

    /** ✅ 확장자 추출 */
    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new IllegalArgumentException("파일 확장자가 없습니다.");
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }
}
