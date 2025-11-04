package com.team404.synco.drive.dto.kafka;

import com.team404.synco.drive.entity.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriveEvent {
    private Long documentSeq;
    private String documentName;
    private String documentType;
    private Long workspaceSeq;
    private String folderName;
    private Long memberSeq;
    private LocalDateTime createdAt;
    private String documentUrl;
    private Long fileSize;
    
    public static DriveEvent fromEntity(Document document) {
        return DriveEvent.builder()
                .documentSeq(document.getDocumentSeq())
                .documentName(document.getDocumentName())
                .documentType(document.getDocumentType().name())
                .workspaceSeq(document.getDriveChannel().getWorkspaceSeq())
                .folderName(document.getFolder() != null ? document.getFolder().getFolderName() : null)
                .memberSeq(document.getMemberSeq())
                .createdAt(document.getCreatedAt())
                .documentUrl(document.getDocumentUrl())
                .fileSize(document.getFileSize())
                .build();
    }
}
