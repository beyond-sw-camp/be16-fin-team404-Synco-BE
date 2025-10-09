package com.team404.synco.drive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class FileUploadReqDto {
    private List<MultipartFile> files;
    private Long parentFolderSeq;
    private Long driveChannelSeq;
}