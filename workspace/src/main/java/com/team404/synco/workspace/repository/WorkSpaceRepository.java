package com.team404.synco.workspace.repository;

import com.team404.synco.common.constant.WorkSpaceType;
import com.team404.synco.member.entity.Member;
import com.team404.synco.workspace.entity.WorkSpace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkSpaceRepository extends JpaRepository<WorkSpace, Long> {
    Optional<WorkSpace> findByMemberAndWorkSpaceType(Member member, WorkSpaceType workSpaceType);
}
