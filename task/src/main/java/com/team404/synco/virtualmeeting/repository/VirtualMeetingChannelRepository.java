package com.team404.synco.virtualmeeting.repository;

import com.team404.synco.virtualmeeting.entity.VirtualMeetingChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VirtualMeetingChannelRepository extends JpaRepository<VirtualMeetingChannel, Long> {
    Optional<VirtualMeetingChannel> findFirstByWorkSpaceSeqOrderByVirtualMeetingChannelSeqAsc(Long workSpaceSeq);

    void deleteAllByWorkSpaceSeq(Long workSpaceSeq);

    List<VirtualMeetingChannel> findByWorkSpaceSeqOrderByVirtualMeetingChannelSeqAsc(Long workSpaceSeq);

    List<VirtualMeetingChannel> findByWorkSpaceSeq(Long workSpaceSeq);
}
