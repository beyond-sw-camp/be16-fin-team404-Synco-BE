package com.team404.synco.task.repository;

import com.team404.synco.task.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // 일반 댓글 페이징 조회 (parentCommentSeq = null)
    @Query("SELECT c FROM Comment c " +
           "JOIN FETCH c.scheduleManagementChannelMember smcm " +
           "WHERE c.task.taskSeq = :taskSeq " +
           "AND c.parentCommentSeq IS NULL " +
           "ORDER BY c.createdAt ASC")
    Page<Comment> findParentCommentsByTaskSeq(@Param("taskSeq") Long taskSeq, Pageable pageable);
    
    // 특정 부모 댓글의 대댓글들 조회
    @Query("SELECT c FROM Comment c " +
           "JOIN FETCH c.scheduleManagementChannelMember smcm " +
           "WHERE c.parentCommentSeq = :parentCommentSeq " +
           "ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentCommentSeq(@Param("parentCommentSeq") Long parentCommentSeq);
}
