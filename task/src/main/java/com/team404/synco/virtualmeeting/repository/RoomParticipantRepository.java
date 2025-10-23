package com.team404.synco.virtualmeeting.repository;

import com.team404.synco.virtualmeeting.entity.RoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomParticipantRepository extends JpaRepository<RoomParticipant, Long> {

}