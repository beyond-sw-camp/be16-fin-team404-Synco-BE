package com.team404.synco.search.service;

import com.team404.synco.search.index.meeting.MeetingSummaryDocument;
import com.team404.synco.search.index.meeting.MeetingSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingIndexService {

    private final MeetingSearchRepository meetingSearchRepository;

    /**
     * MeetingSummary를 Elasticsearch에 인덱싱
     */
    @Transactional
    public void index(MeetingSummaryDocument document) {
        try {
            meetingSearchRepository.save(document);
            log.info("✅ MeetingSummary 인덱싱 완료: id={}", document.getId());
        } catch (Exception e) {
            log.error("❌ MeetingSummary 인덱싱 실패: id={}, error={}", document.getId(), e.getMessage(), e);
        }
    }

    /**
     * MeetingSummary를 Elasticsearch에서 삭제
     */
    @Transactional
    public void delete(Long recordingSummarySeq) {
        try {
            String id = "meeting_" + recordingSummarySeq;
            meetingSearchRepository.deleteById(id);
            log.info("✅ MeetingSummary 삭제 완료: id={}", id);
        } catch (Exception e) {
            log.error("❌ MeetingSummary 삭제 실패: recordingSummarySeq={}, error={}", recordingSummarySeq, e.getMessage(), e);
        }
    }

    /**
     * MeetingSummary를 Elasticsearch에서 업데이트
     */
    @Transactional
    public void update(MeetingSummaryDocument document) {
        index(document);  // Elasticsearch는 save가 upsert로 동작
    }
}

