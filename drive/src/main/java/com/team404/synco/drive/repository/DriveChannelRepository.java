package com.team404.synco.drive.repository;

import com.team404.synco.common.constant.WorkSpaceType;
import com.team404.synco.drive.entity.DriveChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriveChannelRepository extends JpaRepository<DriveChannel, Long> {
    Optional<DriveChannel> findByWorkspaceTypeAndWorkspaceSeq(WorkSpaceType workspaceType, Long workspaceSeq);
}
