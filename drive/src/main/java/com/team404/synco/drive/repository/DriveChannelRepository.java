package com.team404.synco.drive.repository;

import com.team404.synco.drive.entity.DriveChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriveChannelRepository extends JpaRepository<DriveChannel, Long> {
    void deleteByWorkspaceSeq(Long workSpaceSeq);
}
