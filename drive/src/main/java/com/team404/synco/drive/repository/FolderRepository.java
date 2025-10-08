package com.team404.synco.drive.repository;

import com.team404.synco.drive.entity.Folder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long>, JpaSpecificationExecutor<Folder> {
    List<Folder> findByParentFolderSeq(Long parentFolderSeq);

    @Query("SELECT MAX(f.orders) FROM Folder f WHERE f.parentFolderSeq = :parentFolderSeq AND f.driveChannel.driveChannelSeq = :driveChannelSeq")
    Optional<Long> findMaxOrdersByParentFolderSeqAndDriveChannelSeq(@Param("parentFolderSeq") Long parentFolderSeq, @Param("driveChannelSeq") Long driveChannelSeq);

    Optional<Folder> findByFolderNameAndFolderSeqNot(String folderName, Long folderSeq);
}
