package com.team404.synco.virtualmeeting.service;

import com.team404.synco.virtualmeeting.dto.room.CreateRoomRequestDto;
import com.team404.synco.virtualmeeting.dto.room.JoinRoomRequestDto;
import com.team404.synco.virtualmeeting.dto.room.RoomInfoResponseDto;
import com.team404.synco.virtualmeeting.dto.room.LiveKitTokenResponseDto;
import com.team404.synco.virtualmeeting.dto.room.ParticipantInfoDto;
import com.team404.synco.virtualmeeting.entity.Room;
import com.team404.synco.virtualmeeting.entity.RoomParticipant;
import com.team404.synco.common.constant.RoomStatus;
import com.team404.synco.virtualmeeting.repository.RoomParticipantRepository;
import com.team404.synco.virtualmeeting.repository.RoomRepository;
import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RoomService {

    @Value("${livekit.api.key}")
    private String liveKitApiKey;

    @Value("${livekit.api.secret}")
    private String liveKitApiSecret;

     private final RoomRepository roomRepository;
     private final RoomParticipantRepository roomParticipantRepository;
     private final LiveKitService liveKitService;

    /**
     * 화상회의 생성 + 입장
     */
    public RoomInfoResponseDto createAndJoinRoom(CreateRoomRequestDto requestDto, Long memberSeq) {
        log.info("화상회의 생성 + 입장: memberSeq={}, roomName={}, channelSeq={}", 
                memberSeq, requestDto.getRoomName(), requestDto.getChannelSeq());
        
       return null;
    }

    /**
     * 기존 룸에 입장
     */
    public RoomInfoResponseDto joinRoom(String roomId, JoinRoomRequestDto requestDto, Long memberSeq) {
        log.info("룸 입장: roomId={}, memberSeq={}, participantName={}", roomId, memberSeq, requestDto.getParticipantName());
        
       return null;
    }

    /**
     * 룸 퇴장
     */
    public void leaveRoom(String roomId, Long memberSeq) {
        log.info("룸 퇴장: roomId={}, memberSeq={}", roomId, memberSeq);
        
       return;
    }

    /**
     * 룸 종료
     */
    public void endRoom(String roomId, Long memberSeq) {
        log.info("룸 종료: roomId={}, memberSeq={}", roomId, memberSeq);
        
         return;
    }

    /**
     * 룸 정보 조회
     */
    public RoomInfoResponseDto getRoomInfo(String roomId, Long memberSeq) {
        log.info("룸 정보 조회: roomId={}, memberSeq={}", roomId, memberSeq);
        
       return null;
    }

    /**
     * LiveKit 토큰 생성
     */
    public String createToken(String roomId, Long memberSeq) {
        log.info("LiveKit 토큰 생성: roomId={}, memberSeq={}", roomId, memberSeq);

        AccessToken token =  new AccessToken(liveKitApiKey, liveKitApiSecret);
        token.setIdentity(memberSeq.toString());
        token.addGrants(new RoomJoin(true), new RoomName(roomId));

        return token.toJwt();
    }
}
