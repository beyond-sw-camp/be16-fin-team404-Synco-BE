package com.team404.synco.virtualmeeting.repository;

import com.team404.synco.virtualmeeting.entity.Room;
import com.team404.synco.virtualmeeting.entity.RoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomParticipantRepository extends JpaRepository<RoomParticipant, Long> {

    Optional<RoomParticipant> findByRoomAndVirtualMeetingChannelMember_MemberSeq(Room room, long virtualMeetingChannelMemberMemberSeq);
}