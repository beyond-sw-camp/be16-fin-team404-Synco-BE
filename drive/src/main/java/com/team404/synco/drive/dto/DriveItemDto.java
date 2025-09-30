package com.team404.synco.drive.dto;

import com.team404.synco.common.constant.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriveItemDto {
    private Long id;
    private String name;
    private String type; // 'folder', 'file', 'shared-doc'
    private String size;
    private String uploader;
    private LocalDateTime uploadDate;
    private LocalDateTime modifiedDate;
    private String icon;
    private String color;
    private Long parentId;
    private List<DriveItemDto> children;
    
    // 공유문서 관련
    private Boolean isShared;
    private Boolean isLocked;
    private String content;
    
    // 파일 관련
    private String documentUrl;
    private DocumentType documentType;
    private Long memberSeq;
}
