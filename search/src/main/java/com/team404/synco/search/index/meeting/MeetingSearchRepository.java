package com.team404.synco.search.index.meeting;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
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

    @Query("""
    {
      "bool": {
        "should": [
          { "match": { "title":   { "query": "?1" } } },
          { "match": { "content": { "query": "?1" } } }
        ],
        "minimum_should_match": 1,
        "filter": [ { "term": { "workspaceSeq": { "value": ?0 } } } ]
      }
    }
    """)
    Page<MeetingSummaryDocument> searchTitleOrContentByWorkspace(Long workspaceSeq, String keyword, Pageable pageable);
}

