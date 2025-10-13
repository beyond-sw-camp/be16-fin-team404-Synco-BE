package com.team404.synco.drive.repository;

import com.team404.synco.drive.entity.DocumentLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentLineRepository extends JpaRepository<DocumentLine, Long> {
    List<DocumentLine> findByDocumentDocumentSeqOrderByDocumentLineSeq(Long documentSeq);
}
