package com.team404.synco.chat.repository;

import com.team404.synco.chat.entity.ChatChannelMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
