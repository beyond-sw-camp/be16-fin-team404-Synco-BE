package com.team404.synco.task.repository;

import com.team404.synco.task.entity.ScheduleManagementChannelMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleManagementChannelMemberRepository extends JpaRepository<ScheduleManagementChannelMember, Long> {
    void deleteByWorkSpaceSeq(Long workSpaceSeq);
    Optional<ScheduleManagementChannelMember> findByMemberSeqAndWorkSpaceSeq(long memberSeq, long workSpaceSeq);
    Optional<ScheduleManagementChannelMember> findByWorkSpaceSeqAndMemberSeq(Long workSpaceSeq, Long memberSeq);
    Optional<List<ScheduleManagementChannelMember>> findAllByMemberSeq(Long memberSeq);
    Optional<List<ScheduleManagementChannelMember>> findAllByWorkSpaceSeq(Long workSpaceSeq);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ScheduleManagementChannelMember m " +
            "WHERE m.workSpaceSeq = :workSpaceSeq " +
            "AND m.memberSeq = :memberSeq")
    void deleteByChannelAndMember(@Param("workSpaceSeq")Long workSpaceSeq,
                                     @Param("memberSeq")Long memberSeq);

    List<ScheduleManagementChannelMember> findByWorkSpaceSeq(Long workSpaceSeq);
}
