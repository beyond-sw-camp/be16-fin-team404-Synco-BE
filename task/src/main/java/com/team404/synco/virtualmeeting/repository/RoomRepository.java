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
    
    // Room SID로 조회
    Optional<Room> findByRoomSid(String roomSid);
    
    // 채널별 활성 룸 조회
    List<Room> findByChannelSeqAndStatus(Long channelSeq, RoomStatus status);
    
    // 사용자가 생성한 룸 조회
    @Query("SELECT r FROM Room r WHERE r.createdByMemberSeq = :memberSeq ORDER BY r.createdAt DESC")
    List<Room> findByCreatedByMemberSeq(@Param("memberSeq") Long memberSeq);
    
    // 특정 시간 이후 종료된 룸 조회
    @Query("SELECT r FROM Room r WHERE r.status = :status AND r.endedAt > :after")
    List<Room> findEndedRoomsAfter(@Param("status") RoomStatus status, 
                                   @Param("after") LocalDateTime after);
    
    // 채널별 최근 룸 조회
    @Query("SELECT r FROM Room r WHERE r.channelSeq = :channelSeq ORDER BY r.createdAt DESC")
    List<Room> findRecentRoomsByChannel(@Param("channelSeq") Long channelSeq);
    
    // 활성 룸 조회 (참가자 있는 룸)
    @Query("SELECT r FROM Room r WHERE r.status = :status AND EXISTS (SELECT rp FROM RoomParticipant rp WHERE rp.roomSeq = r.roomSeq AND rp.leftAt IS NULL)")
    List<Room> findActiveRoomsWithParticipants(@Param("status") RoomStatus status);
    
    // 룸 이름으로 검색
    @Query("SELECT r FROM Room r WHERE r.name LIKE %:keyword% ORDER BY r.createdAt DESC")
    List<Room> findByNameContaining(@Param("keyword") String keyword);
    
    // 채널별 활성 룸 수 조회
    @Query("SELECT COUNT(r) FROM Room r WHERE r.channelSeq = :channelSeq AND r.status = :status")
    Long countActiveRoomsByChannel(@Param("channelSeq") Long channelSeq, @Param("status") RoomStatus status);
}
