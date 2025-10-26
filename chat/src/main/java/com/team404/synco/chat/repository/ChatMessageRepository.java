package com.team404.synco.chat.repository;

import com.team404.synco.chat.entity.ChatChannel;
import com.team404.synco.chat.entity.ChatMessage;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Long countByChatChannelMember_ChatChannel(ChatChannel chatChannel);
    Long countByChatChannelMember_ChatChannelAndChatMessageSeqGreaterThan(ChatChannel chatChannel, Long chatMessageSeq);

    @Query("SELECT m FROM ChatMessage m " +
            "WHERE m.chatChannelMember.chatChannel.chatChannelSeq = :channelSeq " +
            "AND (:lastId IS NULL OR m.chatMessageSeq < :lastId) " +
            "ORDER BY m.chatMessageSeq DESC")
    List<ChatMessage> findMessages(
            @Param("channelSeq") Long channelSeq,
            @Param("lastId") Long lastId,
            Pageable pageable
    );

    // ✅ 마지막 읽은 메시지 이후 조회
    @Query("""
    SELECT m FROM ChatMessage m
    WHERE m.chatChannelMember.chatChannel.chatChannelSeq = :channelSeq
      AND m.chatMessageSeq > :lastReadSeq
    ORDER BY m.chatMessageSeq ASC
    """)
    List<ChatMessage> findMessagesAfterLastRead(
            @Param("channelSeq") Long channelSeq,
            @Param("lastReadSeq") Long lastReadSeq,
            Pageable pageable);


    // ✅ 최신 메시지 조회 (처음 입장)
    @Query("""
    SELECT m FROM ChatMessage m
    WHERE m.chatChannelMember.chatChannel.chatChannelSeq = :channelSeq
    ORDER BY m.chatMessageSeq DESC
    """)
    List<ChatMessage> findLatestMessages(
            @Param("channelSeq") Long channelSeq,
            Pageable pageable);

    // ✅ 최신 메시지 seq 조회
    @Query("""
    SELECT MAX(m.chatMessageSeq)
    FROM ChatMessage m
    WHERE m.chatChannelMember.chatChannel.chatChannelSeq = :channelSeq
    """)
    Long findLatestSeqByChannel(@Param("channelSeq") Long channelSeq);

}
