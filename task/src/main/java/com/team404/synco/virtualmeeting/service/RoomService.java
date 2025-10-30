package com.team404.synco.virtualmeeting.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team404.synco.common.component.MemberRedisComponent;
import com.team404.synco.common.constant.Authority;
import com.team404.synco.common.constant.RoomStatus;
import com.team404.synco.virtualmeeting.dto.Room.ChatMessageReq;
import com.team404.synco.virtualmeeting.dto.Room.ChatMessageRes;
import com.team404.synco.virtualmeeting.dto.Room.RoomCreateReqDto;
import com.team404.synco.virtualmeeting.dto.MemberInfoDto;
import com.team404.synco.virtualmeeting.dto.Room.RoomSessionResDto;
import com.team404.synco.virtualmeeting.entity.*;
import com.team404.synco.virtualmeeting.entity.Room;
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
import java.util.List;
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
    private final VirtualMeetingChannelRepository virtualMeetingChannelRepository;
    private final RecordingRepository recordingRepository;
    private final MessageRepository messageRepository; // 텍스트 히스토리 DB
    private final RoomServiceClient roomServiceClient; // LiveKit 서버 SDK
    private final EgressServiceClient egressServiceClient; // (선택) 자동 녹화용
    private final ObjectMapper objectMapper;
    private final MemberRedisComponent memberRedisComponent;

    // 화상회의 방 생성
    public RoomSessionResDto createImmediateRoom(Long memberSeq, RoomCreateReqDto roomCreateReqDto) {
        VirtualMeetingChannel virtualMeetingChannel = virtualMeetingChannelRepository.findFirstByWorkSpaceSeq(roomCreateReqDto.getWorkSpaceSeq()).orElseThrow(() -> new EntityNotFoundException("워크스페이스에 속한 화상회의 채널이 없습니다."));
        VirtualMeetingChannelMember virtualMeetingChannelMember = virtualMeetingChannelMemberRepository.findByChannelAndMember(virtualMeetingChannel.getVirtualMeetingChannelSeq(), memberSeq).orElseThrow(() -> new EntityNotFoundException("화상회의 채널 멤버가 아닙니다."));

        if(!((virtualMeetingChannelMember.getAuthority().equals(Authority.SUPER)) || virtualMeetingChannelMember.getAuthority().equals(Authority.MANAGER))){
            throw new IllegalStateException("화상회의 방 생성 권한이 없습니다.");
        }

        // 이미 참여중인 화상회의가 있는지 확인
        List<RoomParticipant> activeParticipants = participantRepository.findByVirtualMeetingChannelMember_MemberSeqAndLeftAtIsNull(memberSeq);
        if(!activeParticipants.isEmpty()){
            throw new IllegalStateException("이미 참여중인 화상회의 방이 있습니다.");
        }

        Room room = roomCreateReqDto.toEntity(memberSeq,virtualMeetingChannelMember.getVirtualMeetingChannel());
        roomRepository.save(room);

        // LiveKit 방 생성
        try{
            roomServiceClient.createRoom(room.getRoomSeq().toString()).execute();
            log.info("LiveKit Room 생성 성공: roomSeq={}", room.getRoomSeq());
        } catch (IOException e){
            log.error("LiveKit Room 생성 실패: {}", e.getMessage());
            throw new RuntimeException("LiveKit Room 생성 실패");
        }

        String token = createToken(room.getRoomSeq(), memberSeq);
        room.startRoom();

        RoomParticipant participant = RoomParticipant.builder()
                .virtualMeetingChannelMember(virtualMeetingChannelMember)
                .room(room)
                .firstJoinedAt(LocalDateTime.now())
                .joinedAt(LocalDateTime.now())
                .displayNameAtJoin(memberRedisComponent.getMemberName(memberSeq))
                .build();
        participantRepository.save(participant);

        return RoomSessionResDto.builder()
                .roomId(room.getRoomSeq())
                .hostId(room.getHostId())
                .token(token)
                .build();
    }

    // 화상회의 방 참여
    public RoomSessionResDto joinRoom(Long memberSeq, Long roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 화상회의 방입니다."));

        VirtualMeetingChannelMember virtualMeetingChannelMember = virtualMeetingChannelMemberRepository.findByChannelAndMember(
                room.getVirtualMeetingChannel().getVirtualMeetingChannelSeq(),
                memberSeq
        ).orElseThrow(()-> new EntityNotFoundException("화상회의 채널 멤버가 아닙니다."));

        if(room.getStatus() != RoomStatus.IN_SESSION){
            throw new IllegalStateException("진행중인 화상회의 방이 아닙니다.");
        }

        // 이미 참여중인 화상회의가 있는지 확인
        List<RoomParticipant> activeParticipants = participantRepository.findByVirtualMeetingChannelMember_MemberSeqAndLeftAtIsNull(memberSeq);
        if(!activeParticipants.isEmpty()){
            throw new IllegalStateException("이미 참여중인 화상회의 방이 있습니다.");
        }

        // LiveKit에서 참가자 조회 - 404는 정상 (참가자가 아직 없음)
        try{
            Response<LivekitModels.ParticipantInfo> response = roomServiceClient.getParticipant(room.getRoomSeq().toString(), memberSeq.toString()).execute();

            // 성공하면 이미 LiveKit에 등록된 참가자
            if(response.isSuccessful()){
                log.warn("이미 LiveKit에 등록된 참가자: roomSeq={}, memberSeq={}", room.getRoomSeq(), memberSeq);
            }
        } catch (IOException e){
            // 404는 정상 상황 (아직 참가 안함)
            log.debug("LiveKit 참가자 조회 완료: roomSeq={}, memberSeq={}", room.getRoomSeq(), memberSeq);
        }

        String memberName = memberRedisComponent.getMemberName(memberSeq);

        // 기존 participant를 room과 memberSeq로 찾기
        Optional<RoomParticipant> existingParticipant = participantRepository.findByRoomAndVirtualMeetingChannelMember_MemberSeq(room, memberSeq);
        
        if(existingParticipant.isPresent()){
            RoomParticipant participant = existingParticipant.get();
            // 이미 참가 중이고 나간 적이 없으면 에러
            if(participant.getLeftAt() == null){
                throw new IllegalStateException("이미 참가한 방입니다.");
            }
            // 다시 참가하는 경우
            participant.joinRoom();
        } else {
            // 새로운 참가자 생성
            RoomParticipant newParticipant = RoomParticipant.builder()
                    .virtualMeetingChannelMember(virtualMeetingChannelMember)
                    .room(room)
                    .firstJoinedAt(LocalDateTime.now())
                    .joinedAt(LocalDateTime.now())
                    .displayNameAtJoin(memberName)
                    .build();
            participantRepository.save(newParticipant);
        }

        String token = createToken(roomId, memberSeq);
        return RoomSessionResDto.builder()
                .roomId(room.getRoomSeq())
                .hostId(room.getHostId())
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

    public void startRecording(Long memberSeq, Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 화상회의 방입니다."));

        // 1. 권한 체크
        if (!room.getHostId().equals(memberSeq)) {
            throw new IllegalStateException("호스트만 녹화를 시작할 수 있습니다.");
        }

        // 2. 이미 녹화 중인지 확인
        if (room.getRecording() != null) {
            throw new IllegalStateException("이미 녹화 중입니다.");
        }

        // 3. LiveKit에 egress 요청
        LivekitEgress.EncodedFileOutput fileOutput = LivekitEgress.EncodedFileOutput.newBuilder()
                .setFileType(LivekitEgress.EncodedFileType.MP4)
                .setFilepath("recordings/{room_name}/{time}.mp4")
                .setS3(s3Upload)
                .build();

        Response<LivekitEgress.EgressInfo> response;
        try {
            response = egressServiceClient.startRoomCompositeEgress(
                    room.getRoomSeq().toString(), // roomName 으로 들어감
                    fileOutput,
                    "speaker", null, null, true
            ).execute();
        } catch (IOException e) {
            log.error("녹화 시작 실패: {}", e.getMessage());
            throw new RuntimeException("녹화 시작 실패");
        }

        if (!response.isSuccessful() || response.body() == null) {
            throw new RuntimeException("LiveKit egress 시작 응답 실패: " + response.code());
        }

        LivekitEgress.EgressInfo egressInfo = response.body();
        String egressId = egressInfo.getEgressId();

        log.info("✅ 녹화 시작 성공: roomSeq={}, egressId={}", room.getRoomSeq(), egressId);

        // 4. Recording 엔티티 생성 & 저장
        Recording recording = Recording.builder()
                .egressId(egressId)
                .room(room)
                .startedAt(LocalDateTime.now()) // 또는 egressInfo.getStartedAt() 변환 가능하면 그걸 사용
                .build();

        room.attachRecording(recording);

        recordingRepository.save(recording);
    }
}
