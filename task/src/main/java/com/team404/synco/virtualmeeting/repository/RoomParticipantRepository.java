package com.team404.synco.virtualmeeting.repository;

import com.team404.synco.virtualmeeting.entity.RoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RoomParticipantRepository extends JpaRepository<RoomParticipant, Long> {
    
    // 룸별 참가자 목록 조회
    List<RoomParticipant> findByRoomSeqOrderByJoinedAtAsc(Long roomSeq);
    
    // 룸별 활성 참가자 조회 (left_at이 null인 참가자)
    @Query("SELECT rp FROM RoomParticipant rp WHERE rp.roomSeq = :roomSeq AND rp.leftAt IS NULL ORDER BY rp.joinedAt ASC")
    List<RoomParticipant> findActiveParticipantsByRoom(@Param("roomSeq") Long roomSeq);
    
    // 채널 멤버별 활성 참가 조회
    @Query("SELECT rp FROM RoomParticipant rp WHERE rp.virtualMeetingChannelMember.channelMemberSeq = :channelMemberSeq AND rp.leftAt IS NULL ORDER BY rp.joinedAt DESC")
    List<RoomParticipant> findActiveParticipationsByChannelMember(@Param("channelMemberSeq") Long channelMemberSeq);
    
    // 특정 시간 이후 참가한 참가자 조회
    @Query("SELECT rp FROM RoomParticipant rp WHERE rp.joinedAt > :after ORDER BY rp.joinedAt DESC")
    List<RoomParticipant> findParticipantsAfter(@Param("after") LocalDateTime after);
    
    // 룸별 연결된 참가자 수 조회
    @Query("SELECT COUNT(rp) FROM RoomParticipant rp WHERE rp.roomSeq = :roomSeq AND rp.leftAt IS NULL")
    Long countActiveParticipantsByRoom(@Param("roomSeq") Long roomSeq);
    
    // 채널 멤버가 특정 룸에 참가 중인지 확인
    @Query("SELECT rp FROM RoomParticipant rp WHERE rp.roomSeq = :roomSeq AND rp.virtualMeetingChannelMember.channelMemberSeq = :channelMemberSeq AND rp.leftAt IS NULL")
    Optional<RoomParticipant> findActiveParticipantInRoom(@Param("roomSeq") Long roomSeq, @Param("channelMemberSeq") Long channelMemberSeq);
    
    // 채널 멤버가 특정 룸에 참가 중인지 확인 (boolean)
    @Query("SELECT COUNT(rp) > 0 FROM RoomParticipant rp WHERE rp.roomSeq = :roomSeq AND rp.virtualMeetingChannelMember.channelMemberSeq = :channelMemberSeq AND rp.leftAt IS NULL")
    boolean existsActiveParticipantInRoom(@Param("roomSeq") Long roomSeq, @Param("channelMemberSeq") Long channelMemberSeq);
    
    // 채널 멤버가 특정 룸에 참가한 적이 있는지 확인
    @Query("SELECT COUNT(rp) > 0 FROM RoomParticipant rp WHERE rp.roomSeq = :roomSeq AND rp.virtualMeetingChannelMember.channelMemberSeq = :channelMemberSeq")
    boolean existsByRoomSeqAndChannelMemberSeq(@Param("roomSeq") Long roomSeq, @Param("channelMemberSeq") Long channelMemberSeq);
    
    // 룸별 특정 채널 멤버의 참가자 조회
    @Query("SELECT rp FROM RoomParticipant rp WHERE rp.roomSeq = :roomSeq AND rp.virtualMeetingChannelMember.channelMemberSeq = :channelMemberSeq")
    Optional<RoomParticipant> findByRoomSeqAndChannelMemberSeq(@Param("roomSeq") Long roomSeq, @Param("channelMemberSeq") Long channelMemberSeq);
    
    // 역할별 참가자 조회
    @Query("SELECT rp FROM RoomParticipant rp WHERE rp.roomSeq = :roomSeq AND rp.roleInMeeting = :role AND rp.leftAt IS NULL ORDER BY rp.joinedAt ASC")
    List<RoomParticipant> findByRoomSeqAndRoleAndActive(@Param("roomSeq") Long roomSeq, @Param("role") String role);
}