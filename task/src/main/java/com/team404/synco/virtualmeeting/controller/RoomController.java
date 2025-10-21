package com.team404.synco.virtualmeeting.controller;

import com.team404.synco.common.constant.dto.ResponseDto;
import com.team404.synco.virtualmeeting.dto.room.CreateRoomRequestDto;
import com.team404.synco.virtualmeeting.dto.room.JoinRoomRequestDto;
import com.team404.synco.virtualmeeting.dto.room.RoomInfoResponseDto;
import com.team404.synco.virtualmeeting.dto.room.LiveKitTokenResponseDto;
import com.team404.synco.virtualmeeting.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;

    /**
     * 화상회의 생성 + 입장 (핵심!)
     */
    @PostMapping("/create-and-join")
    public ResponseEntity<ResponseDto<RoomInfoResponseDto>> createAndJoinRoom(
            @RequestBody CreateRoomRequestDto requestDto,
            @RequestHeader("X-Member-Seq") Long memberSeq) {
        
        log.info("화상회의 생성 + 입장 요청: memberSeq={}, roomName={}", memberSeq, requestDto.getRoomName());
        
        var responseDto = roomService.createAndJoinRoom(requestDto, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok(responseDto, HttpStatus.OK));
    }

    /**
     * 기존 룸에 입장
     */
    @PostMapping("/{roomId}/join")
    public ResponseEntity<ResponseDto<RoomInfoResponseDto>> joinRoom(
            @PathVariable String roomId,
            @RequestBody JoinRoomRequestDto requestDto,
            @RequestHeader("X-Member-Seq") Long memberSeq) {
        
        log.info("룸 입장 요청: roomId={}, memberSeq={}", roomId, memberSeq);
        
        var responseDto = roomService.joinRoom(roomId, requestDto, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok(responseDto, HttpStatus.OK));
    }

    /**
     * 룸 퇴장
     */
    @PostMapping("/{roomId}/leave")
    public ResponseEntity<ResponseDto<String>> leaveRoom(
            @PathVariable String roomId,
            @RequestHeader("X-Member-Seq") Long memberSeq) {
        
        log.info("룸 퇴장 요청: roomId={}, memberSeq={}", roomId, memberSeq);
        
        roomService.leaveRoom(roomId, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok("Successfully left room", HttpStatus.OK));
    }

    /**
     * 룸 종료
     */
    @PostMapping("/{roomId}/end")
    public ResponseEntity<ResponseDto<String>> endRoom(
            @PathVariable String roomId,
            @RequestHeader("X-Member-Seq") Long memberSeq) {
        
        log.info("룸 종료 요청: roomId={}, memberSeq={}", roomId, memberSeq);
        
        roomService.endRoom(roomId, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok("Room ended successfully", HttpStatus.OK));
    }

    /**
     * 룸 정보 조회
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<ResponseDto<RoomInfoResponseDto>> getRoomInfo(
            @PathVariable String roomId,
            @RequestHeader("X-Member-Seq") Long memberSeq) {
        
        log.info("룸 정보 조회 요청: roomId={}, memberSeq={}", roomId, memberSeq);
        
        var responseDto = roomService.getRoomInfo(roomId, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok(responseDto, HttpStatus.OK));
    }

    /**
     * LiveKit 토큰 생성
     */
    @PostMapping("/{roomId}/token")
    public ResponseEntity<ResponseDto<LiveKitTokenResponseDto>> generateLiveKitToken(
            @PathVariable String roomId,
            @RequestHeader("X-Member-Seq") Long memberSeq) {
        
        log.info("LiveKit 토큰 생성 요청: roomId={}, memberSeq={}", roomId, memberSeq);
        
        var responseDto = roomService.generateLiveKitToken(roomId, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok(responseDto, HttpStatus.OK));
    }
}
