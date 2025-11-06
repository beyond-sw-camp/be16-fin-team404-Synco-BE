package com.team404.synco.search.index.chat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatSearchRepository extends ElasticsearchRepository<ChatMessageDocument, String> {
    
    List<ChatMessageDocument> findByContentContaining(String keyword);
    
    Page<ChatMessageDocument> findByWorkspaceSeq(Long workspaceSeq, Pageable pageable);
    
    Page<ChatMessageDocument> findByWorkspaceSeqAndContentContaining(
        Long workspaceSeq,
        String keyword,
        Pageable pageable
    );

    Page<ChatMessageDocument> findByChannelSeq(Long channelSeq, Pageable pageable);
}

