package com.team404.synco.drive.repository;

import com.team404.synco.drive.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByFolderFolderSeq(Long folderSeq);
    List<Document> findByFolderDriveChannelDriveChannelSeqAndFolderParentFolderSeq(Long driveChannelSeq, Long parentFolderSeq);
    
    // 페이지네이션을 위한 메서드들
    Page<Document> findByFolderFolderSeq(Long folderSeq, Pageable pageable);
    Page<Document> findByFolderDriveChannelDriveChannelSeqAndFolderParentFolderSeq(Long driveChannelSeq, Long parentFolderSeq, Pageable pageable);
}
