package com.team404.synco.virtualmeeting.repository;

import com.team404.synco.common.constant.RoomStatus;
import com.team404.synco.virtualmeeting.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoomRepository extends JpaRepository<Room, Long> {
    
    // 채널 시퀀스와 룸 상태로 룸 목록 조회 (페이징) - 관계 기반
    @Query("SELECT r FROM Room r WHERE r.virtualMeetingChannel.virtualMeetingChannelSeq = :channelSeq AND r.status = :status")
    Page<Room> findByChannelSeqAndStatus(@Param("channelSeq") Long channelSeq, 
                                        @Param("status") RoomStatus status, 
                                        Pageable pageable);
}
