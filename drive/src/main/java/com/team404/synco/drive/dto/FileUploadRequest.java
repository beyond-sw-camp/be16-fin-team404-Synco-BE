package com.team404.synco.drive.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class FileUploadRequest {
    private List<MultipartFile> files;
    private Long parentFolderId;
    private Long driveChannelSeq; // 팀 드라이브용
}