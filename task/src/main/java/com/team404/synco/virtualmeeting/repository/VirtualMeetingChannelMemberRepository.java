package com.team404.synco.virtualmeeting.repository;

import com.team404.synco.virtualmeeting.entity.VirtualMeetingChannelMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VirtualMeetingChannelMemberRepository extends JpaRepository<VirtualMeetingChannelMember, Long> {
    Optional<VirtualMeetingChannelMember> findFirstByMemberSeqAndWorkSpaceSeqOrderByIdAsc(Long memberSeq, Long workSpaceSeq);
}
