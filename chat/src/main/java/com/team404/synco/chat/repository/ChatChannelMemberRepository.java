package com.team404.synco.chat.repository;

import com.team404.synco.chat.entity.ChatChannel;
import com.team404.synco.chat.entity.ChatChannelMember;
import com.team404.synco.chat.entity.WorkSpaceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatChannelMemberRepository extends JpaRepository<ChatChannelMember, Long> {
    boolean existsByChatChannelAndMemberSeq(ChatChannel chatChannel, Long memberSeq);
    Optional<ChatChannelMember> findByChatChannelAndMemberSeq(ChatChannel chatChannel, Long memberSeq);
    List<ChatChannelMember> findByMemberSeqAndChatChannel_WorkSpaceType(Long memberSeq, WorkSpaceType workSpaceType);
    List<ChatChannelMember> findByMemberSeqAndChatChannel_WorkSpaceSeq(Long memberSeq, Long workspaceSeq);
    List<ChatChannelMember> findByChatChannel(ChatChannel chatChannel);
}
