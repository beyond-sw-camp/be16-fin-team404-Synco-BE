package com.team404.synco.search.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriveEvent {
    private Long documentSeq;
    private String documentName;
    private String documentType;
    private Long workspaceSeq;
    private String folderName;  // null 가능 (최상위일 경우)
    private Long memberSeq;
    private LocalDateTime createdAt;
    private String documentUrl;
    private Long fileSize;
}

