package com.team404.synco.virtualmeeting.repository;

import com.team404.synco.common.constant.RoomRecordingStatus;
import com.team404.synco.virtualmeeting.entity.RoomRecording;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RoomRecordingRepository extends JpaRepository<RoomRecording, Long> {
    
    // 녹화 ID로 조회
    Optional<RoomRecording> findByRecordingId(String recordingId);
    
    // 룸별 녹화 조회 (1:1 관계이므로 단일 객체)
    Optional<RoomRecording> findByRoomSeq(Long roomSeq);
    
    // 상태별 녹화 목록 조회
    List<RoomRecording> findByRecordingStatusOrderByCreatedAtDesc(RoomRecordingStatus recordingStatus);
    
    // 사용 가능한 녹화 조회
    List<RoomRecording> findByRecordingStatus(RoomRecordingStatus recordingStatus);
    
    // 특정 시간 이후 생성된 녹화 조회
    @Query("SELECT rr FROM RoomRecording rr WHERE rr.createdAt > :after ORDER BY rr.createdAt DESC")
    List<RoomRecording> findRecordingsAfter(@Param("after") LocalDateTime after);
    
    // 룸별 사용 가능한 녹화 조회 (1:1 관계이므로 단일 객체)
    Optional<RoomRecording> findByRoomSeqAndRecordingStatus(Long roomSeq, RoomRecordingStatus recordingStatus);

    // 요약이 있는 녹화 조회
    List<RoomRecording> findBySummaryContentIsNotNullOrderBySummaryCreatedAtDesc();

    // 채널별 요약이 있는 녹화 조회
    @Query("SELECT rr FROM RoomRecording rr JOIN Room r ON rr.roomSeq = r.roomSeq WHERE r.virtualMeetingChannelSeq = :channelSeq AND rr.summaryContent IS NOT NULL ORDER BY rr.summaryCreatedAt DESC")
    List<RoomRecording> findSummarizedRecordingsByChannel(@Param("channelSeq") Long channelSeq);

    // 녹화 서비스별 녹화 조회
    List<RoomRecording> findByRecordingProviderOrderByCreatedAtDesc(String recordingProvider);

    // 완료된 녹화 중 요약이 없는 것 조회
    @Query("SELECT rr FROM RoomRecording rr WHERE rr.recordingStatus = :status AND rr.summaryContent IS NULL")
    List<RoomRecording> findCompletedRecordingsWithoutSummary(@Param("status") RoomRecordingStatus status);
}
