package com.team404.synco.drive.dto;

import com.team404.synco.common.constant.DocumentType;
import com.team404.synco.drive.entity.Document;
import com.team404.synco.drive.entity.Folder;
import com.team404.synco.drive.util.FileTypeClassifier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriveItemDto {
    private Long id; // folderId, fileId, sharedDocId
    private Long parentFolderSeq; // parentFolderId
    private String name; // folderName, fileName, documentName
    private String type; // 'folder', 'file', 'shared-doc'
    private String size; // 파일 크기 (폴더인 경우 null)
    private LocalDateTime uploadDate; // 업로드 날짜 (폴더인 경우 생성 날짜)
    private LocalDateTime modifiedDate; // 수정 날짜 (폴더인 경우 생성 날짜)
    private String icon; // 아이콘 (폴더, 파일, 공유문서 구분)
    private List<DriveItemDto> children; // 하위 아이템 (폴더인 경우에만 사용)
    
    // 공유문서 관련
    private Boolean isShared;
    private Boolean isLocked;

    // 파일 관련
    private String documentUrl;
    private DocumentType documentType;
    private Long memberSeq;
    
    public static DriveItemDto fromFolder(Folder folder) {
        FileTypeClassifier.FolderTypeInfo typeInfo = FileTypeClassifier.FolderTypeInfo.of(folder);
        
        return DriveItemDto.builder()
                .id(folder.getFolderSeq())
                .name(folder.getFolderName())
                .type(typeInfo.type)
                .size(typeInfo.size)
                .uploadDate(folder.getCreatedAt())
                .modifiedDate(folder.getUpdatedAt())
                .icon(typeInfo.icon)
                .parentFolderSeq(folder.getParentFolderSeq())
                .children(new ArrayList<>())
                .build();
    }
    
    public static DriveItemDto fromDocument(Document document) {
        FileTypeClassifier.DocumentTypeInfo typeInfo = FileTypeClassifier.DocumentTypeInfo.of(document);
        
        return DriveItemDto.builder()
                .id(document.getDocumentSeq())
                .name(document.getDocumentName())
                .type(typeInfo.type)
                .size(typeInfo.size)
                .uploadDate(document.getCreatedAt())
                .modifiedDate(document.getUpdatedAt())
                .icon(typeInfo.icon)
                .parentFolderSeq(document.getFolder() != null ? document.getFolder().getFolderSeq() : null)
                .isShared(typeInfo.isShared)
                .isLocked(typeInfo.isLocked)
                .documentUrl(document.getDocumentUrl())
                .documentType(document.getDocumentType())
                .memberSeq(document.getMemberSeq())
                .build();
    }
}
