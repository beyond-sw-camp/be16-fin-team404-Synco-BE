package com.team404.synco.search.index.task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskSearchRepository extends ElasticsearchRepository<TaskDocument, String> {
    
    List<TaskDocument> findByTitleContaining(String keyword);
    
    Page<TaskDocument> findByWorkspaceSeq(Long workspaceSeq, Pageable pageable);
    
    Page<TaskDocument> findByWorkspaceSeqAndTitleContaining(
        Long workspaceSeq, 
        String keyword, 
        Pageable pageable
    );

    // 짧은 키워드일 때 접두(prefix) 매칭용 (title + content 대상)
    @Query("""
        {
          "bool": {
            "should": [
              { "match_phrase_prefix": { "title":   { "query": "?1" } } },
              { "match_phrase_prefix": { "content": { "query": "?1" } } }
            ],
            "minimum_should_match": 1,
            "filter": [ { "term": { "workspaceSeq": { "value": ?0 } } } ]
          }
        }
        """)
    Page<TaskDocument> searchTitleOrContentPrefixByWorkspace(Long workspaceSeq, String keyword, Pageable pageable);

    // 긴 키워드일 때 일반 match (title + content 대상)
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
    Page<TaskDocument> searchTitleOrContentByWorkspace(Long workspaceSeq, String keyword, Pageable pageable);
}

