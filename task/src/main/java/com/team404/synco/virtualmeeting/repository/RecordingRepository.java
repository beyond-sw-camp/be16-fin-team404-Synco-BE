package com.team404.synco.virtualmeeting.repository;

import com.team404.synco.common.constant.RecordingStatus;
import com.team404.synco.virtualmeeting.entity.Recording;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RecordingRepository extends JpaRepository<Recording, Long> {
    
    // Room으로 녹화 조회 (1:1 관계)
    Optional<Recording> findByRoomSeq(Long roomSeq);
    
    // Egress ID로 녹화 조회
    Optional<Recording> findByEgressId(String egressId);
    
    // 상태별 녹화 목록 조회
    List<Recording> findByStatusOrderByCreatedAtDesc(RecordingStatus status);
    
    // 특정 시간 이후 생성된 녹화 조회
    @Query("SELECT r FROM Recording r WHERE r.createdAt > :after ORDER BY r.createdAt DESC")
    List<Recording> findRecordingsAfter(@Param("after") LocalDateTime after);
    
    // 완료된 녹화 조회
    List<Recording> findByStatusOrderByEndedAtDesc(RecordingStatus status);
    
    // 채널별 녹화 조회
    @Query("SELECT r FROM Recording r JOIN Room room ON r.roomSeq = room.roomSeq WHERE room.channelSeq = :channelSeq ORDER BY r.createdAt DESC")
    List<Recording> findByChannelSeq(@Param("channelSeq") Long channelSeq);
    
    // 녹화 시간 범위로 조회
    @Query("SELECT r FROM Recording r WHERE r.startedAt BETWEEN :startTime AND :endTime ORDER BY r.startedAt DESC")
    List<Recording> findByStartedAtBetween(@Param("startTime") LocalDateTime startTime, 
                                          @Param("endTime") LocalDateTime endTime);
}
