package com.team404.synco.virtualmeeting.repository;

import com.team404.synco.virtualmeeting.entity.Recording;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecordingRepository extends JpaRepository<Recording, Long> {
    
    Optional<Recording> findByEgressId(String egressId);

    Optional<Recording> findByRoom_RoomSeq(Long roomRoomSeq);
}
