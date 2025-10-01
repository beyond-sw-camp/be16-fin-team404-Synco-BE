package com.team404.synco.drive.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadRequest {
    
    private List<MultipartFile> files;
    private Long driveChannelSeq;
    private Long parentFolderId;
}
