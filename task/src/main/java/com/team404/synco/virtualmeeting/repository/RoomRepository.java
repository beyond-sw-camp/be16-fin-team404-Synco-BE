package com.team404.synco.virtualmeeting.repository;

import com.team404.synco.common.constant.RoomStatus;
import com.team404.synco.virtualmeeting.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
    // 채널 시퀀스와 룸 상태로 룸 목록 조회 (페이징)
    Page<Room> findByChannelSeqAndStatus(Long channelSeq, RoomStatus status, Pageable pageable);
}
