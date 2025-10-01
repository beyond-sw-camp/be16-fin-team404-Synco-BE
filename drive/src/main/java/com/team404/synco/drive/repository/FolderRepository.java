package com.team404.synco.drive.repository;

import com.team404.synco.drive.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {
    List<Folder> findByParentFolderSeq(Long parentFolderSeq);
    List<Folder> findByDriveChannelDriveChannelSeqAndParentFolderSeq(Long driveChannelSeq, Long parentFolderSeq);
}
