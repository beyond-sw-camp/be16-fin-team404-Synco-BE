package com.team404.synco.virtualmeeting.controller;

import com.team404.synco.common.constant.dto.ResponseDto;
import com.team404.synco.virtualmeeting.dto.Room.ChatMessageReq;
import com.team404.synco.virtualmeeting.dto.Room.ChatMessageRes;
import com.team404.synco.virtualmeeting.dto.Room.RoomCreateReqDto;
import com.team404.synco.virtualmeeting.dto.Room.RoomSessionResDto;
import com.team404.synco.virtualmeeting.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;

    // 화상회의 방 생성
    @PostMapping("/create")
    public ResponseEntity<ResponseDto<?>> createRoom(@RequestHeader("X-Member-Seq") Long memberId,
                                                     @RequestBody RoomCreateReqDto roomCreateReqDto) {

        RoomSessionResDto res = roomService.createImmediateRoom(memberId, roomCreateReqDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.ok(res, HttpStatus.CREATED));
    }

    // 화상회의 방 참여
    @PostMapping("/{roomId}/join")
    public ResponseEntity<ResponseDto<?>> joinRoom(@RequestHeader("X-Member-Seq") Long memberId,
                                                   @PathVariable("roomId") Long roomId) {
        RoomSessionResDto res = roomService.joinRoom(memberId,roomId);
        return ResponseEntity.ok(ResponseDto.ok(res, HttpStatus.OK));
    }

    // 화상회의 방 취소
    @DeleteMapping("/{roomId}/cancel")
    public ResponseEntity<ResponseDto<?>> cancelRoom(@RequestHeader("X-Member-Seq") Long memberId,
                                                     @PathVariable("roomId") Long roomId) {
        roomService.cancelRoom(memberId, roomId);
        return ResponseEntity.ok(ResponseDto.ok("회의 취소", HttpStatus.OK));
    }

    // 채팅 메시지 전송
    @PostMapping("/{roomId}/messages")
    public ResponseEntity<ResponseDto<?>> sendMessage(@RequestHeader("X-Member-Seq") Long memberId,
                                                      @PathVariable Long roomId,
                                                      @RequestBody ChatMessageReq req) {
        roomService.sendMessage(memberId, roomId, req); // DB 저장 + SendData("chat")
        return ResponseEntity.accepted().body(ResponseDto.ok("전송", HttpStatus.ACCEPTED));
    }

    // 채팅 목록 조회
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<ResponseDto<?>> getMessageList(@RequestHeader("X-Member-Seq") Long memberId,
                                          @PathVariable Long roomId,
                                          @PageableDefault(value = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ChatMessageRes> res = roomService.getMessageList(memberId, roomId, pageable);
        return ResponseEntity.ok(ResponseDto.ok(res, HttpStatus.OK));
    }
}
