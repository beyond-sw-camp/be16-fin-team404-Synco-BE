package com.team404.synco.search.index.drive;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriveSearchRepository extends ElasticsearchRepository<DriveDocument, String> {
    
    List<DriveDocument> findByTitleContaining(String keyword);
    
    Page<DriveDocument> findByWorkspaceSeq(Long workspaceSeq, Pageable pageable);
    
    Page<DriveDocument> findByWorkspaceSeqAndTitleContaining(
        Long workspaceSeq,
        String keyword,
        Pageable pageable
    );
}

