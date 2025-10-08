package com.team404.synco.drive.repository;

import com.team404.synco.drive.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {
    List<Document> findByFolderFolderSeq(Long folderSeq);

    Page<Document> findByFolderDriveChannelDriveChannelSeqAndFolderParentFolderSeq(Long driveChannelSeq, Long parentFolderSeq, Pageable pageable);
}
