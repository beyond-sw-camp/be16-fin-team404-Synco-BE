package com.team404.synco.virtualmeeting.repository;

import com.team404.synco.virtualmeeting.entity.VirtualMeetingChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VirtualMeetingChannelRepository extends JpaRepository<VirtualMeetingChannel, Long> {
}
