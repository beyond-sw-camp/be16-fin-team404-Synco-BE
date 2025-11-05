package com.team404.synco.search.index.task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskSearchRepository extends ElasticsearchRepository<TaskDocument, String> {

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

