package com.team404.synco.drive.repository;

import com.team404.synco.drive.entity.Document;
import com.team404.synco.drive.entity.DriveChannel;
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
public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {
    List<Document> findByFolderFolderSeq(Long folderSeq);

    // 문서명 중복 체크
    Optional<Document> findByDocumentNameAndDocumentSeqNotAndFolderDriveChannelAndFolderFolderSeq(String documentName, Long documentSeq, DriveChannel driveChannel, Long folderSeq);
    
    // 최상위 문서 중복 검증을 위한 메서드 (folder가 null인 경우)
    @Query("SELECT d FROM Document d WHERE d.documentName = :documentName AND d.documentSeq != :documentSeq AND d.folder IS NULL AND d.driveChannel.driveChannelSeq = :driveChannelSeq")
    Optional<Document> findTopLevelDocumentByNameAndChannel(@Param("documentName") String documentName, @Param("documentSeq") Long documentSeq, @Param("driveChannelSeq") Long driveChannelSeq);
    
    // 페이징 쿼리: 특정 드라이브 채널과 부모 폴더의 문서들 조회
    @Query("SELECT d FROM Document d WHERE d.driveChannel.driveChannelSeq = :driveChannelSeq AND " +
           "(:parentFolderSeq IS NULL AND d.folder IS NULL OR d.folder.folderSeq = :parentFolderSeq)")
    Page<Document> findDocumentsByDriveChannelAndParent(@Param("driveChannelSeq") Long driveChannelSeq, 
                                                       @Param("parentFolderSeq") Long parentFolderSeq, 
                                                       Pageable pageable);
}
