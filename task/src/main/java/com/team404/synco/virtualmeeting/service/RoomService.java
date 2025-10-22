package com.team404.synco.virtualmeeting.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team404.synco.common.constant.RoomStatus;
import com.team404.synco.virtualmeeting.dto.Room.ChatMessageReq;
import com.team404.synco.virtualmeeting.dto.Room.ChatMessageRes;
import com.team404.synco.virtualmeeting.dto.Room.RoomCreateReqDto;
import com.team404.synco.virtualmeeting.dto.Room.RoomSessionResDto;
import com.team404.synco.virtualmeeting.entity.Message;
import com.team404.synco.virtualmeeting.entity.Room;
import com.team404.synco.virtualmeeting.entity.RoomParticipant;
import com.team404.synco.virtualmeeting.entity.VirtualMeetingChannelMember;
import com.team404.synco.virtualmeeting.repository.*;
import io.livekit.server.*;
import jakarta.persistence.EntityNotFoundException;
import livekit.LivekitEgress;
import livekit.LivekitModels;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RoomService {

    private final LivekitEgress.S3Upload s3Upload;
    private final VirtualMeetingChannelMemberRepository virtualMeetingChannelMemberRepository;
    @Value("${livekit.api.key}")
    private String liveKitApiKey;

    @Value("${livekit.api.secret}")
    private String liveKitApiSecret;

    private final RoomRepository roomRepository;
    private final RoomParticipantRepository participantRepository;
    private final MessageRepository messageRepository; // 텍스트 히스토리 DB
    private final RoomServiceClient roomServiceClient; // LiveKit 서버 SDK
    private final EgressServiceClient egressServiceClient; // (선택) 자동 녹화용
    private final ObjectMapper objectMapper;
    private final VirtualMeetingRedisService virtualMeetingRedisService;

    // 화상회의 방 생성
    public RoomSessionResDto createImmediateRoom(Long memberSeq, RoomCreateReqDto roomCreateReqDto) {
        Room room = roomCreateReqDto.toEntity(memberSeq);
        roomRepository.save(room);

        createAutoEgressRoom(room);
        String token = createToken(room.getRoomSeq(), memberSeq);
        room.startRoom();

        return RoomSessionResDto.builder()
                .roomId(room.getRoomSeq())
                .token(token)
                .build();
    }

    // 화상회의 방 참여
    public RoomSessionResDto joinRoom(Long memberSeq, Long roomId) {
        VirtualMeetingChannelMember virtualMeetingChannelMember = virtualMeetingChannelMemberRepository.findById(memberSeq).orElseThrow(() -> new EntityNotFoundException("화상회의 채널 멤버가 아닙니다."));
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 화상회의 방입니다."));

        if(room.getStatus() != RoomStatus.IN_SESSION){
            throw new IllegalStateException("진행중인 화상회의 방이 아닙니다.");
        }

        Response<LivekitModels.ParticipantInfo> response;
        try{
            response = roomServiceClient.getParticipant(room.getRoomSeq().toString(), memberSeq.toString()).execute();
        } catch (IOException e){
            log.error(e.getMessage());
            throw new RuntimeException("LiveKit 참가자 조회 실패");
        }

        if(!response.isSuccessful()){
            throw new IllegalStateException("화상회의 방에 참가할 수 없습니다.");
        }

        String memberName = virtualMeetingRedisService.getMemberName(memberSeq);

        Optional<RoomParticipant> participant = participantRepository.findById(memberSeq);
        if(participant.isEmpty()){
            RoomParticipant newParticipant = RoomParticipant.builder()
                    .virtualMeetingChannelMember(virtualMeetingChannelMember)
                    .room(room)
                    .firstJoinedAt(LocalDateTime.now())
                    .joinedAt(LocalDateTime.now())
                    .displayNameAtJoin(memberName)
                    .build();
            participantRepository.save(newParticipant);
        } else{
            RoomParticipant existingParticipant = participant.get();
            existingParticipant.joinRoom();
        }

        String token = createToken(roomId, memberSeq);
        return RoomSessionResDto.builder()
                .roomId(room.getRoomSeq())
                .token(token)
                .build();
    }

    // 화상회의 방 취소
    public void cancelRoom(Long memberSeq, Long roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 화상회의 방입니다."));
        if (!room.getHostId().equals(memberSeq)) {
            throw new IllegalStateException("화상회의 방 호스트가 아닙니다.");
        }
        if (room.getStatus() != RoomStatus.WAITING) {
            throw new IllegalStateException("취소할 수 없는 화상회의 방입니다.");
        }
        roomRepository.delete(room);
    }

    // 화상회의 채팅 전송
    public void sendMessage(Long memberId, Long roomId, ChatMessageReq chatMessageReq) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 화상회의 방입니다."));

        if(room.getStatus() != RoomStatus.IN_SESSION){
            throw new IllegalStateException("진행중인 화상회의 방이 아닙니다.");
        }
        Response<LivekitModels.ParticipantInfo> response;
        try{
            response = roomServiceClient.getParticipant(room.getRoomSeq().toString(), memberId.toString()).execute();
        } catch (IOException e){
            log.error(e.getMessage());
            throw new RuntimeException("LiveKit 참가자 조회 실패");
        }
        if(!response.isSuccessful() || LivekitModels.ParticipantInfo.State.DISCONNECTED.equals(Objects.requireNonNull(response.body()).getState())){
            throw new IllegalStateException("화상회의 방에 참가중인 멤버가 아닙니다.");
        }

        String data;
        try{
            data = objectMapper.writeValueAsString(chatMessageReq);
        } catch(JsonProcessingException e){
            log.error(e.getMessage());
            throw new RuntimeException("채팅 메시지 직렬화 실패");
        }

        roomServiceClient.sendData(
                roomId.toString(),
                data.getBytes(),
                LivekitModels.DataPacket.Kind.RELIABLE,
                Collections.emptyList(),
                Collections.emptyList(),
                "chat").enqueue(new Callback<Void>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    log.info("LiveKit 채팅 메시지 전송 성공: roomId={}, memberId={}", roomId, memberId);
                    // DB에 채팅 메시지 저장
                    Message message = Message.builder()
                            .room(room)
                            .senderId(memberId)
                            .content(chatMessageReq.getContent())
                            .build();
                    messageRepository.save(message);
                } else {
                    log.error("LiveKit 채팅 메시지 전송 실패: HTTP {}", response.code());
                }
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<Void> call, Throwable throwable) {
                log.error("LiveKit 채팅 메시지 전송 실패: {}", throwable.getMessage());
            }
        });
    }

    // 화상회의 채팅 히스토리 조회
    @Transactional(readOnly = true)
    public Page<ChatMessageRes> getMessageList(Long memberId, Long roomId ,Pageable pageable) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 화상회의 방입니다."));

        if (room.getStatus() != RoomStatus.IN_SESSION)
            throw new RuntimeException("진행 중인 화상회의가 아닙니다.");

        Response<LivekitModels.ParticipantInfo> res;
        try {
            res = roomServiceClient.getParticipant(roomId.toString(), memberId.toString()).execute();
        } catch (IOException e) {
            throw new RuntimeException("화상회의 통신 실패" + e.getMessage());
        }
        if (!res.isSuccessful() || LivekitModels.ParticipantInfo.State.DISCONNECTED == Objects.requireNonNull(res.body()).getState())
            throw new EntityNotFoundException("참여 중인 화상회의가 아닙니다.");
        Page<Message> page = messageRepository.findAllByRoom(room, pageable);
        return page.map(ChatMessageRes::fromEntity);
    }

    // LiveKit 토큰 생성
    public String createToken(Long roomId, Long memberSeq) {
        log.info("LiveKit 토큰 생성: roomId={}, memberSeq={}", roomId, memberSeq);

        AccessToken token =  new AccessToken(liveKitApiKey, liveKitApiSecret);
        token.setIdentity(memberSeq.toString());
        token.addGrants(new RoomJoin(true), new RoomName(roomId.toString()));

        return token.toJwt();
    }

    private void createAutoEgressRoom(Room room) {
        try{
            roomServiceClient.createRoom(room.getRoomSeq().toString()).execute();
        } catch (IOException e){
            log.error(e.getMessage());
            throw new RuntimeException("LiveKit Room 생성 실패");
        }

        LivekitEgress.EncodedFileOutput fileOutput = LivekitEgress.EncodedFileOutput.newBuilder()
                .setFileType(LivekitEgress.EncodedFileType.MP4)
                .setFilepath("recordings/{room_name}/{time}.mp4")
                .setS3(s3Upload)
                .build();

        try{
            egressServiceClient.startRoomCompositeEgress(
                            room.getRoomSeq().toString(),
                            fileOutput,
                            "speaker")
                    .execute();
        } catch (IOException e){
            log.error(e.getMessage());
            throw new RuntimeException("LiveKit 자동 녹화 시작 실패");
        }
    }
}
