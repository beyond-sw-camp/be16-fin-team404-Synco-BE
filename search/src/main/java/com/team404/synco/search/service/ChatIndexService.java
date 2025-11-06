package com.team404.synco.search.service;

import com.team404.synco.search.index.chat.ChatMessageDocument;
import com.team404.synco.search.index.chat.ChatSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatIndexService {

    private final ChatSearchRepository chatSearchRepository;

    /**
     * ChatMessage를 Elasticsearch에 인덱싱
     */
    @Transactional
    public void index(ChatMessageDocument document) {
        try {
            chatSearchRepository.save(document);
            log.info("✅ ChatMessage 인덱싱 완료: id={}", document.getId());
        } catch (Exception e) {
            log.error("❌ ChatMessage 인덱싱 실패: id={}, error={}", document.getId(), e.getMessage(), e);
        }
    }

    /**
     * ChatMessage를 Elasticsearch에서 삭제
     */
    @Transactional
    public void delete(Long chatMessageSeq) {
        try {
            String id = "chat_" + chatMessageSeq;
            chatSearchRepository.deleteById(id);
            log.info("✅ ChatMessage 삭제 완료: id={}", id);
        } catch (Exception e) {
            log.error("❌ ChatMessage 삭제 실패: chatMessageSeq={}, error={}", chatMessageSeq, e.getMessage(), e);
        }
    }

    /**
     * ChatMessage를 Elasticsearch에서 업데이트
     */
    @Transactional
    public void update(ChatMessageDocument document) {
        index(document);  // Elasticsearch는 save가 upsert로 동작
    }

    /**
     * 특정 채널의 모든 문서에 대해 channelName을 일괄 업데이트
     */
    @Transactional
    public void updateChannelNameForChannel(Long channelSeq, String newChannelName) {
        try {
            Page<ChatMessageDocument> page = chatSearchRepository.findByChannelSeq(channelSeq, org.springframework.data.domain.Pageable.unpaged());
            page.getContent().forEach(doc -> {
                ChatMessageDocument updated = ChatMessageDocument.builder()
                        .id(doc.getId())
                        .chatMessageSeq(doc.getChatMessageSeq())
                        .content(doc.getContent())
                        .workspaceSeq(doc.getWorkspaceSeq())
                        .channelSeq(doc.getChannelSeq())
                        .channelName(newChannelName)
                        .memberSeq(doc.getMemberSeq())
                        .createdAt(doc.getCreatedAt())
                        .build();
                chatSearchRepository.save(updated);
            });
            log.info("✅ 채널명 일괄 업데이트 완료: channelSeq={}, newName={}", channelSeq, newChannelName);
        } catch (Exception e) {
            log.error("❌ 채널명 일괄 업데이트 실패: channelSeq={}, error={}", channelSeq, e.getMessage(), e);
        }
    }

    /**
     * 특정 채널에 속한 모든 채팅 문서 삭제
     */
    @Transactional
    public void deleteByChannelSeq(Long channelSeq) {
        try {
            Page<ChatMessageDocument> page = chatSearchRepository.findByChannelSeq(channelSeq, org.springframework.data.domain.Pageable.unpaged());
            page.getContent().forEach(doc -> chatSearchRepository.deleteById(doc.getId()));
            log.info("✅ 채널 전체 문서 삭제 완료: channelSeq={}, count={}", channelSeq, page.getNumberOfElements());
        } catch (Exception e) {
            log.error("❌ 채널 전체 문서 삭제 실패: channelSeq={}, error={}", channelSeq, e.getMessage(), e);
        }
    }
}

