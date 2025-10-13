package com.team404.synco.drive.dto;

import com.team404.synco.common.constant.YnColumn;
import com.team404.synco.drive.entity.Document;
import com.team404.synco.drive.entity.DocumentLine;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDetailDto {
    private Long id; // documentSeq
    private String name; // documentName
    private Boolean isLocked; // 잠금 상태
    private List<String> content; // 문서 내용 (라인별)
    
    /**
     * DocumentLine 리스트에서 DTO 생성 (DB 조회 시)
     */
    public static DocumentDetailDto fromEntity(Document document, List<DocumentLine> documentLines) {
        List<String> content = documentLines.stream()
                .map(DocumentLine::getDocumentContent)
                .toList();
        
        return DocumentDetailDto.builder()
                .id(document.getDocumentSeq())
                .name(document.getDocumentName())
                .isLocked(YnColumn.IS_TRUE.equals(document.getYnLock()))
                .content(content)
                .build();
    }
    
    /**
     * 텍스트 컨텐츠에서 DTO 생성 (Redis 캐시 HIT 시)
     */
    public static DocumentDetailDto fromEntityWithContent(Document document, String textContent) {
        // 텍스트를 라인별로 분할
        List<String> content = textContent.isEmpty() 
            ? List.of() 
            : Arrays.asList(textContent.split("\n", -1));
        
        return DocumentDetailDto.builder()
                .id(document.getDocumentSeq())
                .name(document.getDocumentName())
                .isLocked(YnColumn.IS_TRUE.equals(document.getYnLock()))
                .content(content)
                .build();
    }
}
