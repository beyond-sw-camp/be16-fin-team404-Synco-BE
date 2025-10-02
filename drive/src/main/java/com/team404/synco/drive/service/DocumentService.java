package com.team404.synco.drive.service;

import com.team404.synco.common.constant.WorkSpaceType;
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
    private final CommonDriveService commonDriveService;

    // 공유문서 상세 조회
    public DriveItemDto getDocument(Long documentSeq) {
        Document document = documentRepository.findById(documentSeq)
            .orElseThrow(() -> new EntityNotFoundException("문서를 찾을 수 없습니다."));
        
        // 드라이브 타입에 따라 적절한 변환 메서드 사용
        WorkSpaceType workspaceType = document.getFolder().getDriveChannel().getWorkspaceType();
        return commonDriveService.convertDocumentToDto(document, workspaceType);
    }

    // 문서 내용 업데이트
    // TODO: 실시간 문서편집 기능 추가시 수정 필요. 현재는 기존 라인 전체 삭제 후 새로 저장.
    public DriveItemDto updateDocumentContent(Long documentSeq, UpdateDocumentRequest request) {
        Document document = documentRepository.findById(documentSeq)
            .orElseThrow(() -> new EntityNotFoundException("문서를 찾을 수 없습니다."));
        
        // 내용 업데이트
        if (request.getContent() != null) {
            updateDocumentContent(document, request.getContent());
        }
        
        Document savedDocument = documentRepository.save(document);
        WorkSpaceType workspaceType = savedDocument.getFolder().getDriveChannel().getWorkspaceType();
        return commonDriveService.convertDocumentToDto(savedDocument, workspaceType);
    }

    // 문서 잠금/해제 토글
    public DriveItemDto toggleDocumentLock(Long documentSeq) {
        Document document = documentRepository.findById(documentSeq).orElseThrow(() -> new EntityNotFoundException("문서를 찾을 수 없습니다."));
        
        // 개인 드라이브에서는 잠금 기능 불필요
        WorkSpaceType workspaceType = document.getFolder().getDriveChannel().getWorkspaceType();
        if (workspaceType == WorkSpaceType.INDIVIDUAL) {
            log.warn("개인 드라이브 문서는 잠금 기능을 사용할 수 없습니다: documentSeq={}", documentSeq);
            // 개인 드라이브에서는 잠금 상태를 변경하지 않고 그대로 반환
        } else {
            // 팀 드라이브에서만 잠금 토글
            String currentLockStatus = document.getYnLock();
            String newLockStatus = YnColumn.IS_TRUE.equals(currentLockStatus) ? YnColumn.IS_FALSE : YnColumn.IS_TRUE;
            document.updateLockStatus(newLockStatus);
        }
        
        Document savedDocument = documentRepository.save(document);
        return commonDriveService.convertDocumentToDto(savedDocument, workspaceType);
    }

    // 문서 다운로드
    public ResponseEntity<byte[]> downloadDocument(Long documentSeq) {
        Document document = documentRepository.findById(documentSeq).orElseThrow(() -> new EntityNotFoundException("문서를 찾을 수 없습니다."));
        
        try {
            // 문서 내용을 바이트 배열로 변환
            String content = getDocumentContent(document);
            byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", document.getDocumentName() + ".txt");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(contentBytes);
                
        } catch (Exception e) {
            log.error("문서 다운로드 실패: {}", document.getDocumentName(), e);
            throw new RuntimeException("문서 다운로드에 실패했습니다.", e);
        }
    }


    // 문서 내용 업데이트
    // TODO: 실시간 문서편집 기능 추가시 수정 필요. 현재는 기존 라인 전체 삭제.
    private void updateDocumentContent(Document document, String content) {
        try {
            List<DocumentLine> existingLines = documentLineRepository.findByDocumentDocumentSeqOrderByDocumentLineSeq(document.getDocumentSeq());
            documentLineRepository.deleteAll(existingLines);
            
            // 새 내용을 라인별로 저장
            String[] lines = content.split("\n");
            List<DocumentLine> newLines = new ArrayList<>();
            
            for (int i = 0; i < lines.length; i++) {
                DocumentLine line = DocumentLine.builder()
                    .documentContent(lines[i])
                    .documentLineSeq((long) (i + 1))
                    .document(document)
                    .build();
                newLines.add(line);
            }
            
            documentLineRepository.saveAll(newLines);
            
        } catch (Exception e) {
            log.error("문서 내용 업데이트 실패", e);
            throw new RuntimeException("문서 내용 업데이트에 실패했습니다.", e);
        }
    }

    // 문서 내용 조회
    private String getDocumentContent(Document document) {
        try {
            List<DocumentLine> documentLines = documentLineRepository.findByDocumentDocumentSeqOrderByDocumentLineSeq(document.getDocumentSeq());

            if (documentLines.isEmpty()) {
                return "문서 내용이 없습니다.";
            }

            return documentLines.stream()
                .map(DocumentLine::getDocumentContent)
                .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            log.error("문서 내용 조회 실패", e);
            return "문서 내용을 불러올 수 없습니다.";
        }
    }
}
