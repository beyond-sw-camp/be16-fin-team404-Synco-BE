package com.team404.synco.drive.service;

import com.team404.synco.drive.dto.DocumentDetailDto;
import com.team404.synco.drive.entity.Document;
import com.team404.synco.drive.entity.DocumentLine;
import com.team404.synco.drive.repository.DocumentLineRepository;
import com.team404.synco.drive.repository.DocumentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Redis -> DB 동기화 서비스
 * YJS 전체 문서를 라인별 DocumentLine으로 변환하여 저장
 */
@Service
@Slf4j
public class DocumentSyncService {

    private final DocumentRepository documentRepository;
    private final DocumentLineRepository documentLineRepository;
    private final RedisTemplate<String, String> documentContentsTemplate;
    
    // Redis 키 패턴
    private static final String DOCUMENT_CONTENT_KEY = "document:content:";
    private static final String DOCUMENT_DIRTY_FLAG_KEY = "document:dirty:";

    public DocumentSyncService(DocumentRepository documentRepository, DocumentLineRepository documentLineRepository, @Qualifier("documentContents")RedisTemplate<String, String> documentContentsTemplate) {
        this.documentRepository = documentRepository;
        this.documentLineRepository = documentLineRepository;
        this.documentContentsTemplate = documentContentsTemplate;
    }

    /**
     * 5분마다 변경된 문서를 Redis에서 DB로 동기화
     */
    @Scheduled(fixedRate = 300000) // 5분 = 300,000ms
    @Transactional
    public void syncDocumentsToDatabase() {
        log.info("📝 문서 동기화 시작");
        
        try {
            // 변경된 문서 목록 조회 (dirty flag가 있는 문서들)
            Set<String> dirtyDocuments = documentContentsTemplate.keys(DOCUMENT_DIRTY_FLAG_KEY + "*");
            
            if (dirtyDocuments == null || dirtyDocuments.isEmpty()) {
                log.info("✅ 동기화할 문서 없음");
                return;
            }
            
            int successCount = 0;
            int failCount = 0;
            
            for (String dirtyKey : dirtyDocuments) {
                // "document:dirty:123" -> "123"
                String documentIdStr = dirtyKey.replace(DOCUMENT_DIRTY_FLAG_KEY, "");
                
                try {
                    Long documentId = Long.parseLong(documentIdStr);
                    syncSingleDocument(documentId);
                    successCount++;
                    
                    // 동기화 완료 후 dirty flag 제거
                    documentContentsTemplate.delete(dirtyKey);
                    
                } catch (NumberFormatException e) {
                    log.error("❌ 잘못된 문서 ID 형식: {}", documentIdStr);
                    failCount++;
                } catch (Exception e) {
                    log.error("❌ 문서 동기화 실패 - DocumentId: {}", documentIdStr, e);
                    failCount++;
                }
            }
            
            log.info("✅ 문서 동기화 완료 - 성공: {}, 실패: {}", successCount, failCount);
            
        } catch (Exception e) {
            log.error("❌ 문서 동기화 작업 실패", e);
        }
    }

    /**
     * 단일 문서 동기화
     * Redis의 전체 텍스트를 라인별로 분할하여 DB에 저장
     */
    @Transactional
    public void syncSingleDocument(Long documentId) {
        log.debug("📝 문서 동기화 중 - DocumentId: {}", documentId);
        
        // 1. Redis에서 문서 내용 조회
        String contentKey = DOCUMENT_CONTENT_KEY + documentId;
        String fullContent = documentContentsTemplate.opsForValue().get(contentKey);
        
        if (fullContent == null) {
            log.warn("⚠️ Redis에 문서 내용 없음 - DocumentId: {}", documentId);
            return;
        }
        
        // 2. Document 엔티티 조회
        Document document = documentRepository.findById(documentId).orElseThrow(() -> new EntityNotFoundException("문서를 찾을 수 없습니다: " + documentId));
        
        // 3. 기존 라인들 삭제 (전체 교체 방식)
        List<DocumentLine> existingLines = documentLineRepository.findByDocumentDocumentSeqOrderByDocumentLineSeq(documentId);
        
        if (!existingLines.isEmpty()) {
            documentLineRepository.deleteAll(existingLines);
            log.debug("🗑️ 기존 라인 삭제 - DocumentId: {}, 라인 수: {}", documentId, existingLines.size());
        }
        
        // 4. 텍스트를 라인별로 분할
        // Windows(\r\n), Unix(\n), Mac(\r) 모두 지원
        String normalizedContent = fullContent.replace("\r\n", "\n").replace("\r", "\n");
        String[] lines = normalizedContent.split("\n", -1); // -1: 빈 줄도 포함
        
        // 5. 새 라인들 생성 및 저장
        List<DocumentLine> newLines = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            DocumentLine line = DocumentLine.builder()
                .document(document)
                .documentLineSeq((long) (i + 1))
                .documentParentLineSeq(i == 0 ? 0 : i)
                .documentContent(lines[i])
                .build();
            newLines.add(line);
        }
        
        documentLineRepository.saveAll(newLines);
        
        log.info("✅ 문서 동기화 완료 - DocumentId: {}, 라인 수: {}", documentId, lines.length);
    }

    /**
     * Redis에서 문서 내용 조회 (캐시 조회)
     */
    public String getDocumentContentFromCache(Long documentId) {
        String key = DOCUMENT_CONTENT_KEY + documentId;
        String value = documentContentsTemplate.opsForValue().get(key);
        
        if (value != null) {
            log.debug("✅ 캐시 HIT - DocumentId: {}", documentId);
        }
        
        return value;
    }

    /**
     * DocumentLine 리스트를 텍스트로 변환
     */
    public String loadDocumentLinesAsText(List<DocumentLine> lines) {
        if (lines.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            sb.append(lines.get(i).getDocumentContent());
            if (i < lines.size() - 1) {
                sb.append("\n");
            }
        }
        
        return sb.toString();
    }

    /**
     * Redis에 캐시 저장 (조회 시 캐시 미스일 때)
     */
    public void cacheDocumentContent(Long documentId, String content) {
        String key = DOCUMENT_CONTENT_KEY + documentId;
        documentContentsTemplate.opsForValue().set(key, content);
        log.debug("💾 Redis 캐시 저장 - DocumentId: {}", documentId);
    }

    /**
     * 문서 내용 업데이트 (YJS 편집 시)
     * Redis 저장 + dirty flag 자동 설정
     */
    public void updateDocumentContent(Long documentId, String textContent) {
        // Redis에 저장
        String contentKey = DOCUMENT_CONTENT_KEY + documentId;
        documentContentsTemplate.opsForValue().set(contentKey, textContent);
        
        // Dirty flag 설정 (5분 후 DB 동기화 대상)
        String dirtyKey = DOCUMENT_DIRTY_FLAG_KEY + documentId;
        documentContentsTemplate.opsForValue().set(dirtyKey, "1");
        
        log.debug("📝 문서 업데이트 + Dirty Flag 설정 - DocumentId: {}, 길이: {}", documentId, textContent.length());
    }
}


