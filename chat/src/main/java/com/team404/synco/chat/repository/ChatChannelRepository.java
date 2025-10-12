package com.team404.synco.chat.repository;

import com.team404.synco.chat.entity.ChatChannel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatChannelRepository extends JpaRepository<ChatChannel, Long> {
    Optional<ChatChannel> findByChatChannelSeqAndWorkSpaceSeq(Long chatChannelSeq, Long workSpaceSeq);
}
