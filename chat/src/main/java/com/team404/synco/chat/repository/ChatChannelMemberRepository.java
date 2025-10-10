package com.team404.synco.chat.repository;

import com.team404.synco.chat.entity.ChatChannelMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatChannelMemberRepository extends JpaRepository<ChatChannelMember, Long> {
    Optional<ChatChannelMember> findFirstByMemberSeqAndWorkSpaceSeqOrderByChatChannelSeqAsc(Long memberSeq, Long workSpaceSeq);
}
