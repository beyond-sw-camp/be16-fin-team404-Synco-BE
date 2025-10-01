package com.team404.synco.drive.service;

import com.team404.synco.common.constant.DocumentType;
import com.team404.synco.common.constant.YnColumn;
import com.team404.synco.drive.dto.DriveItemDto;
import com.team404.synco.drive.dto.UpdateDocumentRequest;
import com.team404.synco.drive.entity.Document;
import com.team404.synco.drive.entity.DocumentLine;
import com.team404.synco.drive.repository.DocumentLineRepository;
import com.team404.synco.drive.repository.DocumentRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentLineRepository documentLineRepository;

    // 공유문서 상세 조회
    public DriveItemDto getDocument(Long documentSeq) {
        Document document = documentRepository.findById(documentSeq)
            .orElseThrow(() -> new EntityNotFoundException("문서를 찾을 수 없습니다."));
        
        return convertDocumentToDto(document);
    }

    // 문서 내용 업데이트
    public DriveItemDto updateDocumentContent(Long documentSeq, UpdateDocumentRequest request) {
        Document document = documentRepository.findById(documentSeq)
            .orElseThrow(() -> new EntityNotFoundException("문서를 찾을 수 없습니다."));
        
        // 내용 업데이트
        if (request.getContent() != null) {
            updateDocumentContent(document, request.getContent());
        }
        
        Document savedDocument = documentRepository.save(document);
        return convertDocumentToDto(savedDocument);
    }

    /**
     * 문서 잠금/해제 토글
     */
    public DriveItemDto toggleDocumentLock(Long documentSeq) {
        Document document = documentRepository.findById(documentSeq)
            .orElseThrow(() -> new EntityNotFoundException("문서를 찾을 수 없습니다."));
        
        boolean isCurrentlyLocked = YnColumn.IS_TRUE.equals(document.getYnLock());
        document.updateLockStatus(isCurrentlyLocked ? YnColumn.IS_FALSE : YnColumn.IS_TRUE);
        
        Document savedDocument = documentRepository.save(document);
        return convertDocumentToDto(savedDocument);
    }

    /**
     * 문서 다운로드 (텍스트 파일)
     */
    public ResponseEntity<byte[]> downloadDocument(Long documentSeq) {
        Document document = documentRepository.findById(documentSeq)
            .orElseThrow(() -> new EntityNotFoundException("문서를 찾을 수 없습니다."));
        
        String content = getDocumentContent(document);
        byte[] fileContent = content.getBytes(StandardCharsets.UTF_8);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", document.getDocumentName() + ".txt");
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(fileContent);
    }

    // Helper Methods

    private DriveItemDto convertDocumentToDto(Document document) {
        boolean isShared = DocumentType.CUSTOM.equals(document.getDocumentType());
        String content = getDocumentContent(document);
        
        return DriveItemDto.builder()
            .id(document.getDocumentSeq())
            .name(document.getDocumentName())
            .type(isShared ? "shared-doc" : "file")
            .size(isShared ? "-" : "0 KB")
            .uploadDate(document.getCreatedAt())
            .modifiedDate(document.getUpdatedAt())
            .icon(isShared ? "mdi-file-document-multiple" : "mdi-file")
            .parentId(document.getFolder() != null ? document.getFolder().getFolderSeq() : null)
            .isShared(isShared)
            .isLocked(YnColumn.IS_TRUE.equals(document.getYnLock()))
            .content(content)
            .documentUrl(document.getDocumentUrl())
            .documentType(document.getDocumentType())
            .memberSeq(document.getMemberSeq())
            .build();
    }

    private String getDocumentContent(Document document) {
        try {
            List<DocumentLine> documentLines = documentLineRepository.findByDocumentDocumentSeqOrderByDocumentLineSeq(document.getDocumentSeq());
            
            if (documentLines.isEmpty()) {
                return "문서를 작성해보세요...";
            }
            
            return documentLines.stream()
                .map(DocumentLine::getDocumentContent)
                .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            log.error("문서 내용 조회 실패", e);
            return "문서 내용을 불러올 수 없습니다.";
        }
    }

    private void updateDocumentContent(Document document, String content) {
        // 기존 DocumentLine 삭제
        List<DocumentLine> existingLines = documentLineRepository.findByDocumentDocumentSeqOrderByDocumentLineSeq(document.getDocumentSeq());
        documentLineRepository.deleteAll(existingLines);
        
        // 새로운 내용을 DocumentLine으로 저장
        if (content != null && !content.trim().isEmpty()) {
            String[] lines = content.split("\n");
            List<DocumentLine> newLines = new ArrayList<>();
            
            for (int i = 0; i < lines.length; i++) {
                DocumentLine documentLine = DocumentLine.builder()
                    .documentParentLineSeq(0L)
                    .documentContent(lines[i])
                    .document(document)
                    .build();
                newLines.add(documentLine);
            }
            
            documentLineRepository.saveAll(newLines);
        }
    }
}
