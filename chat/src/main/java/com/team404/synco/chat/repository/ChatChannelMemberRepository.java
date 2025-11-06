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
    List<ChatChannelMember> findByChatChannel(ChatChannel chatChannel);

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

    // 특정 사용자가 속한 모든 1:1 채팅 채널 목록 조회
    List<ChatChannelMember> findByMemberSeqAndChatChannel_WorkSpaceType(Long memberSeq, WorkSpaceType workSpaceType);

    // 1:1 참여자 모두가 속한 채널 반환
    @Query("""
    SELECT ccm.chatChannel.chatChannelSeq 
    FROM ChatChannelMember ccm
    WHERE ccm.chatChannel.workSpaceSeq = :workSpaceSeq
      AND ccm.chatChannel.workSpaceType = :workSpaceType
      AND ccm.memberSeq IN (:memberSeq1, :memberSeq2)
    GROUP BY ccm.chatChannel.chatChannelSeq
    HAVING COUNT(DISTINCT ccm.memberSeq) = 2
    """)
    Optional<Long> findExistingIndividualChannel(
            @Param("workSpaceSeq") Long workSpaceSeq,
            @Param("workSpaceType") WorkSpaceType workSpaceType,
            @Param("memberSeq1") Long memberSeq1,
            @Param("memberSeq2") Long memberSeq2
    );
}
