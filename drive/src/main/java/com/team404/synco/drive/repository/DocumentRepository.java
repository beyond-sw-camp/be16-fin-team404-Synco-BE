package com.team404.synco.drive.repository;

import com.team404.synco.drive.entity.Document;
import com.team404.synco.drive.entity.DocumentLine;
import com.team404.synco.drive.entity.DriveChannel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByFolderFolderSeq(Long folderSeq);

    // 문서명 중복 체크 (폴더 내)
    Optional<Document> findByDocumentNameAndDocumentSeqNotAndDriveChannelAndFolderFolderSeq(String documentName, Long documentSeq, DriveChannel driveChannel, Long folderSeq);
    
    // 최상위 문서 중복 검증 (새 파일 생성용)
    @Query("SELECT d FROM Document d WHERE d.documentName = :documentName AND d.folder IS NULL AND d.driveChannel.driveChannelSeq = :driveChannelSeq")
    Optional<Document> findTopLevelDocumentByNameAndChannelForNewFile(@Param("documentName") String documentName, @Param("driveChannelSeq") Long driveChannelSeq);
    
    // 최상위 문서 중복 검증 (이름 변경용 - 기존 문서 제외)
    @Query("SELECT d FROM Document d WHERE d.documentName = :documentName AND d.documentSeq != :documentSeq AND d.folder IS NULL AND d.driveChannel.driveChannelSeq = :driveChannelSeq")
    Optional<Document> findTopLevelDocumentByNameAndChannelExcluding(@Param("documentName") String documentName, @Param("documentSeq") Long documentSeq, @Param("driveChannelSeq") Long driveChannelSeq);
    
    // 페이징 쿼리: 특정 드라이브 채널과 부모 폴더의 문서들 조회
    @Query("SELECT d FROM Document d WHERE d.driveChannel.driveChannelSeq = :driveChannelSeq AND " +
           "(:parentFolderSeq IS NULL AND d.folder IS NULL OR d.folder.folderSeq = :parentFolderSeq)")
    Page<Document> findDocumentsByDriveChannelAndParent(@Param("driveChannelSeq") Long driveChannelSeq, 
                                                       @Param("parentFolderSeq") Long parentFolderSeq, 
                                                       Pageable pageable);
    
    Optional<Document> findByDocumentSeqAndDriveChannelDriveChannelSeq(Long documentSeq, Long driveChannelSeq);
}
