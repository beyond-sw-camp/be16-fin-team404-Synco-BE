package com.team404.synco.search.index.meeting;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingSearchRepository extends ElasticsearchRepository<MeetingSummaryDocument, String> {
    
    List<MeetingSummaryDocument> findByTitleContaining(String keyword);
    
    Page<MeetingSummaryDocument> findByWorkspaceSeq(Long workspaceSeq, Pageable pageable);
    
    Page<MeetingSummaryDocument> findByWorkspaceSeqAndTitleContaining(
        Long workspaceSeq,
        String keyword,
        Pageable pageable
    );
}

