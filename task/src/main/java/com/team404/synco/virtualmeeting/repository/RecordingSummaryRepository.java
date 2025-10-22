package com.team404.synco.virtualmeeting.repository;

import com.team404.synco.virtualmeeting.entity.RecordingSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RecordingSummaryRepository extends JpaRepository<RecordingSummary, Long> {
    
    // Recording으로 요약 조회 (1:1 관계)
    Optional<RecordingSummary> findByRecordingSeq(Long recordingSeq);

    // 특정 시간 이후 생성된 요약 조회
    @Query("SELECT rs FROM RecordingSummary rs WHERE rs.createdAt > :after ORDER BY rs.createdAt DESC")
    List<RecordingSummary> findSummariesAfter(@Param("after") LocalDateTime after);
    
    // 내용이 있는 요약만 조회
    @Query("SELECT rs FROM RecordingSummary rs WHERE rs.content IS NOT NULL AND rs.content != '' ORDER BY rs.createdAt DESC")
    List<RecordingSummary> findSummariesWithContent();
    
    // 채널별 요약 조회
    @Query("SELECT rs FROM RecordingSummary rs JOIN Recording r ON rs.recordingSeq = r.recordingSeq JOIN Room room ON r.roomSeq = room.roomSeq WHERE room.channelSeq = :channelSeq ORDER BY rs.createdAt DESC")
    List<RecordingSummary> findByChannelSeq(@Param("channelSeq") Long channelSeq);
    
    // 제목으로 검색
    @Query("SELECT rs FROM RecordingSummary rs WHERE rs.title LIKE %:keyword% ORDER BY rs.createdAt DESC")
    List<RecordingSummary> findByTitleContaining(@Param("keyword") String keyword);
    
    // 내용으로 검색
    @Query("SELECT rs FROM RecordingSummary rs WHERE rs.content LIKE %:keyword% ORDER BY rs.createdAt DESC")
    List<RecordingSummary> findByContentContaining(@Param("keyword") String keyword);
}
