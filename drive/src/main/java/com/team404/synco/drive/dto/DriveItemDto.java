package com.team404.synco.drive.dto;

import com.team404.synco.common.constant.DocumentType;
import lombok.*;

import java.time.LocalDateTime;
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
    private String content;
    
    // 파일 관련
    private String documentUrl;
    private DocumentType documentType;
    private Long memberSeq;
}
