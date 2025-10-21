package com.team404.synco.virtualmeeting.repository;

import com.team404.synco.common.constant.ParticipantStatus;
import com.team404.synco.virtualmeeting.entity.RoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RoomParticipantRepository extends JpaRepository<RoomParticipant, Long> {
    
    // Participant ID로 조회
    Optional<RoomParticipant> findByParticipantId(String participantId);
    
    // Participant SID로 조회
    Optional<RoomParticipant> findByParticipantSid(String participantSid);
    
    // 룸별 참가자 목록 조회
    List<RoomParticipant> findByRoomSeqOrderByJoinedAtAsc(Long roomSeq);
    
    // 룸별 활성 참가자 조회
    List<RoomParticipant> findByRoomSeqAndParticipantStatusOrderByJoinedAtAsc(Long roomSeq, ParticipantStatus status);
    
    // 멤버별 활성 참가 조회
    List<RoomParticipant> findByMemberSeqAndParticipantStatus(Long memberSeq, ParticipantStatus status);
    
    // 특정 시간 이후 참가한 참가자 조회
    @Query("SELECT rp FROM RoomParticipant rp WHERE rp.joinedAt > :after ORDER BY rp.joinedAt DESC")
    List<RoomParticipant> findParticipantsAfter(@Param("after") LocalDateTime after);
    
    // 룸별 연결된 참가자 수 조회
    @Query("SELECT COUNT(rp) FROM RoomParticipant rp WHERE rp.roomSeq = :roomSeq AND rp.participantStatus = :status")
    Long countConnectedParticipantsByRoom(@Param("roomSeq") Long roomSeq, @Param("status") ParticipantStatus status);
    
    // 채널별 활성 참가자 조회
    @Query("SELECT rp FROM RoomParticipant rp JOIN Room r ON rp.roomSeq = r.roomSeq WHERE r.virtualMeetingChannelSeq = :channelSeq AND rp.participantStatus = :status")
    List<RoomParticipant> findActiveParticipantsByChannel(@Param("channelSeq") Long channelSeq, @Param("status") ParticipantStatus status);
    
    // 멤버가 특정 룸에 참가 중인지 확인
    @Query("SELECT rp FROM RoomParticipant rp WHERE rp.roomSeq = :roomSeq AND rp.memberSeq = :memberSeq AND rp.participantStatus = :status")
    Optional<RoomParticipant> findActiveParticipantInRoom(@Param("roomSeq") Long roomSeq, @Param("memberSeq") Long memberSeq, @Param("status") ParticipantStatus status);
}
