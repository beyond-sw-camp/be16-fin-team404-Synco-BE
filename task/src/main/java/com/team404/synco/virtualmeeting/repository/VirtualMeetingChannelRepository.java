package com.team404.synco.virtualmeeting.repository;

import com.team404.synco.virtualmeeting.entity.VirtualMeetingChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VirtualMeetingChannelRepository extends JpaRepository<VirtualMeetingChannel, Long> {
    Optional<VirtualMeetingChannel> findByVirtualMeetingChannelSeqAndWorkSpaceSeq(Long virtualMeetingChannelSeq, Long workSpaceSeq);
    void deleteAllByWorkSpaceSeq(Long workSpaceSeq);
}
