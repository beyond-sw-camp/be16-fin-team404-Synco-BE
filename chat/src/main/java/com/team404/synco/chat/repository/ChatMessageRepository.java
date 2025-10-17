package com.team404.synco.chat.repository;

import com.team404.synco.chat.entity.ChatChannel;
import com.team404.synco.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Long countByChatChannelMember_ChatChannel(ChatChannel chatChannel);
    Long countByChatChannelMember_ChatChannelAndChatMessageSeqGreaterThan(ChatChannel chatChannel, Long chatMessageSeq);
}
