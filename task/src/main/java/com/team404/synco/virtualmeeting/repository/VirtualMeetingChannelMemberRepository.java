package com.team404.synco.virtualmeeting.repository;

import com.team404.synco.virtualmeeting.entity.VirtualMeetingChannelMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VirtualMeetingChannelMemberRepository extends JpaRepository<VirtualMeetingChannelMember, Long> {
    @Query("SELECT m FROM VirtualMeetingChannelMember m " +
            "WHERE m.virtualMeetingChannel.virtualMeetingChannelSeq = :virtualMeetingChannelSeq " +
            "AND m.memberSeq = :memberSeq")
    Optional<VirtualMeetingChannelMember> findByChannelAndMember(@Param("virtualMeetingChannelSeq") Long virtualMeetingChannelSeq,
                                                                 @Param("memberSeq") Long memberSeq);

    @Query("SELECT COUNT(m) > 0 FROM VirtualMeetingChannelMember m " +
            "WHERE m.virtualMeetingChannel.virtualMeetingChannelSeq = :virtualMeetingChannelSeq " +
            "AND m.memberSeq = :virtualMeetingChannelSeq")
    boolean existsMember(@Param("virtualMeetingChannelSeq") Long virtualMeetingChannelSeq, @Param("memberSeq") Long memberSeq);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM VirtualMeetingChannelMember m " +
            "WHERE m.virtualMeetingChannel.virtualMeetingChannelSeq = :virtualMeetingChannelSeq " +
            "AND m.memberSeq = :memberSeq")
    void deleteByChannelAndMember(@Param("virtualMeetingChannelSeq")Long virtualMeetingChannelSeq,
                                                      @Param("memberSeq")Long memberSeq);

    Optional<VirtualMeetingChannelMember> findByMemberSeq(long memberSeq);
}