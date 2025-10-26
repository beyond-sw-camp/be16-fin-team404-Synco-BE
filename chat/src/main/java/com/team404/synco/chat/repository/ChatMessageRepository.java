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

}
