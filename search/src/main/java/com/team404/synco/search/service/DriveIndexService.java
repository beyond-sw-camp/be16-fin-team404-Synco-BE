package com.team404.synco.search.service;

import com.team404.synco.search.index.drive.DriveDocument;
import com.team404.synco.search.index.drive.DriveSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriveIndexService {

    private final DriveSearchRepository driveSearchRepository;

    /**
     * Drive를 Elasticsearch에 인덱싱
     */
    @Transactional
    public void index(DriveDocument document) {
        try {
            driveSearchRepository.save(document);
            log.info("✅ Drive 인덱싱 완료: id={}", document.getId());
        } catch (Exception e) {
            log.error("❌ Drive 인덱싱 실패: id={}, error={}", document.getId(), e.getMessage(), e);
        }
    }

    /**
     * Drive를 Elasticsearch에서 삭제
     */
    @Transactional
    public void delete(Long documentSeq) {
        try {
            String id = "drive_" + documentSeq;
            driveSearchRepository.deleteById(id);
            log.info("✅ Drive 삭제 완료: id={}", id);
        } catch (Exception e) {
            log.error("❌ Drive 삭제 실패: documentSeq={}, error={}", documentSeq, e.getMessage(), e);
        }
    }

    /**
     * Drive를 Elasticsearch에서 업데이트
     */
    @Transactional
    public void update(DriveDocument document) {
        index(document);  // Elasticsearch는 save가 upsert로 동작
    }
}

