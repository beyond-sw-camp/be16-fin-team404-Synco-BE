package com.team404.synco.virtualmeeting.repository;

import com.team404.synco.common.constant.RoomStatus;
import com.team404.synco.virtualmeeting.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    
    // Room ID로 조회
    Optional<Room> findByRoomId(String roomId);
    
    // Room SID로 조회
    Optional<Room> findByRoomSid(String roomSid);
    
    // 채널별 활성 룸 조회
    List<Room> findByVirtualMeetingChannelSeqAndRoomStatus(
            Long virtualMeetingChannelSeq, RoomStatus roomStatus);
    
    // 사용자가 생성한 룸 조회
    List<Room> findByCreatedByMemberSeqOrderByCreatedAtDesc(Long memberSeq);
    
    // 특정 시간 이후 종료된 룸 조회
    @Query("SELECT r FROM Room r WHERE r.roomStatus = :status AND r.endedAt > :after")
    List<Room> findEndedRoomsAfter(@Param("status") RoomStatus status, 
                                   @Param("after") LocalDateTime after);
    
    // 녹화가 활성화된 룸 조회
    List<Room> findByRecordingEnabledTrueAndRoomStatus(RoomStatus roomStatus);
    
    // 채널별 최근 룸 조회
    @Query("SELECT r FROM Room r WHERE r.virtualMeetingChannelSeq = :channelSeq ORDER BY r.createdAt DESC")
    List<Room> findRecentRoomsByChannel(@Param("channelSeq") Long channelSeq);
    
    // 활성 룸 조회 (참가자 있는 룸)
    @Query("SELECT r FROM Room r WHERE r.roomStatus = :status AND EXISTS (SELECT rp FROM RoomParticipant rp WHERE rp.roomSeq = r.roomSeq AND rp.participantStatus = 'CONNECTED')")
    List<Room> findActiveRoomsWithParticipants(@Param("status") RoomStatus status);
}
