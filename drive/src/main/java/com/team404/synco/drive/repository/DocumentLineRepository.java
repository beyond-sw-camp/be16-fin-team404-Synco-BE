package com.team404.synco.drive.repository;

import com.team404.synco.drive.entity.DocumentLine;
import com.team404.synco.drive.entity.DriveChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentLineRepository extends JpaRepository<DocumentLine, Long> {
    List<DocumentLine> findByDocumentDocumentSeqOrderByDocumentLineSeq(Long documentSeq);

    Optional<DocumentLine> findByFeId(String feId);

    Optional<DocumentLine> findByPrevFeId(String prevFeId);
}
