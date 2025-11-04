package com.team404.synco.search.service;

import com.team404.synco.search.index.task.TaskDocument;
import com.team404.synco.search.index.task.TaskSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskIndexService {

    private final TaskSearchRepository taskSearchRepository;

    /**
     * Task를 Elasticsearch에 인덱싱
     */
    @Transactional
    public void index(TaskDocument document) {
        try {
            taskSearchRepository.save(document);
            log.info("✅ Task 인덱싱 완료: id={}", document.getId());
        } catch (Exception e) {
            log.error("❌ Task 인덱싱 실패: id={}, error={}", document.getId(), e.getMessage(), e);
        }
    }

    /**
     * Task를 Elasticsearch에서 삭제
     */
    @Transactional
    public void delete(Long taskSeq) {
        try {
            String id = "task_" + taskSeq;
            taskSearchRepository.deleteById(id);
            log.info("✅ Task 삭제 완료: id={}", id);
        } catch (Exception e) {
            log.error("❌ Task 삭제 실패: taskSeq={}, error={}", taskSeq, e.getMessage(), e);
        }
    }

    /**
     * Task를 Elasticsearch에서 업데이트
     */
    @Transactional
    public void update(TaskDocument document) {
        index(document);  // Elasticsearch는 save가 upsert로 동작
    }
}

