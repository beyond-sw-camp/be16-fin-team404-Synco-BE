package com.team404.synco.chat.repository;

import com.team404.synco.chat.entity.ChatChannel;
import com.team404.synco.chat.entity.ChatChannelMember;
import com.team404.synco.chat.entity.WorkSpaceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
            "WHERE m.chatChannel.chatChannelSeq = :chatChannelSeq " +
            "AND m.memberSeq = :memberSeq")
    boolean existsMember(@Param("chatChannelSeq") Long chatChannelSeq, @Param("memberSeq") Long memberSeq);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ChatChannelMember m " +
            "WHERE m.chatChannel.chatChannelSeq = :chatChannelSeq " +
            "AND m.memberSeq = :memberSeq")
    void deleteByChannelAndMember(@Param("chatChannelSeq") Long chatChannelSeq, @Param("memberSeq") Long memberSeq);

    boolean existsByChatChannelAndMemberSeq(ChatChannel chatChannel, Long memberSeq);
    Optional<ChatChannelMember> findByChatChannelAndMemberSeq(ChatChannel chatChannel, Long memberSeq);
    List<ChatChannelMember> findByMemberSeqAndChatChannel_WorkSpaceType(Long memberSeq, WorkSpaceType workSpaceType);
    List<ChatChannelMember> findByChatChannel(ChatChannel chatChannel);
//    @Query("""
//    select ccm.lastReadChatMessageSeq
//      from ChatChannelMember ccm
//     where ccm.chatChannel.chatChannelSeq = :channelSeq
//       and ccm.memberSeq = :memberSeq
//    """)
//    Long findLastReadSeq(Long channelSeq, Long memberSeq);
    Optional<ChatChannelMember> findByChatChannel_ChatChannelSeqAndMemberSeq(Long channelSeq, Long memberSeq);

    // ✅ 마지막 읽은 메시지 업데이트
    @Modifying
    @Query("""
    UPDATE ChatChannelMember ccm
       SET ccm.lastReadChatMessageSeq = :latestSeq
     WHERE ccm.memberSeq = :memberSeq
       AND ccm.chatChannel.chatChannelSeq = :channelSeq
    """)
    int updateLastRead(
            @Param("memberSeq") Long memberSeq,
            @Param("channelSeq") Long channelSeq,
            @Param("latestSeq") Long latestSeq
    );

}
