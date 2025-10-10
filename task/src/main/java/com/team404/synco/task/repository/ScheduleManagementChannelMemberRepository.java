package com.team404.synco.task.repository;

import com.team404.synco.task.entity.ScheduleManagementChannelMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleManagementChannelMemberRepository extends JpaRepository<ScheduleManagementChannelMember, Long> {
    void deleteByWorkSpaceSeq(Long workSpaceSeq);
}
