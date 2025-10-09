package com.team404.synco.drive.repository;

import com.team404.synco.drive.entity.Document;
import com.team404.synco.drive.entity.DriveChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {
    List<Document> findByFolderFolderSeq(Long folderSeq);

    Optional<Document> findByDocumentNameAndFolderDriveChannelAndFolderFolderSeq(String documentName, DriveChannel driveChannel, Long folderSeq);

    Optional<Document> findByDocumentNameAndDocumentSeqNotAndFolderDriveChannelAndFolderFolderSeq(String documentName, Long documentSeq, DriveChannel driveChannel, Long folderSeq);
}
