package com.team404.synco.chat.repository;

import com.team404.synco.chat.entity.ChatChannel;
import com.team404.synco.chat.entity.ChatChannelMember;
import com.team404.synco.chat.entity.WorkSpaceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import java.util.List;
import java.util.Optional;

public interface ChatChannelMemberRepository extends JpaRepository<ChatChannelMember, Long> {
    @Query("SELECT m FROM ChatChannelMember m " +
            "WHERE m.chatChannel.chatChannelSeq = :chatChannelSeq " +
            "AND m.memberSeq = :memberSeq")
    Optional<ChatChannelMember> findByChannelAndMember(@Param("chatChannelSeq") Long chatChannelSeq,
                                                       @Param("memberSeq") Long memberSeq);

    @Query("SELECT COUNT(m) > 0 FROM ChatChannelMember m " +
            "WHERE m.chatChannel.chatChannelSeq = :channelSeq " +
            "AND m.memberSeq = :memberSeq")
    boolean existsMember(@Param("channelSeq") Long channelSeq, @Param("memberSeq") Long memberSeq);
    boolean existsByChatChannelAndMemberSeq(ChatChannel chatChannel, Long memberSeq);
    Optional<ChatChannelMember> findByChatChannelAndMemberSeq(ChatChannel chatChannel, Long memberSeq);
    List<ChatChannelMember> findByMemberSeqAndChatChannel_WorkSpaceType(Long memberSeq, WorkSpaceType workSpaceType);
    List<ChatChannelMember> findByMemberSeqAndChatChannel_WorkSpaceSeq(Long memberSeq, Long workspaceSeq);
    List<ChatChannelMember> findByChatChannel(ChatChannel chatChannel);
}
