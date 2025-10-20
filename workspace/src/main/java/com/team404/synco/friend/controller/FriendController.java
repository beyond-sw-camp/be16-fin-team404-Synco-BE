package com.team404.synco.friend.controller;

import com.team404.synco.common.dto.ResponseDto;
import com.team404.synco.friend.dto.FriendReqDto;
import com.team404.synco.friend.dto.FriendResDto;
import com.team404.synco.friend.dto.ReceivedReqDto;
import com.team404.synco.friend.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/friend")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    // 1. 친구 요청 보내기
    @PostMapping("/request")
    public ResponseEntity<ResponseDto<?>> requestFriend(
            @RequestHeader("X-Member-Seq") Long memberSeq,
            @RequestBody FriendReqDto friendReqDto) {
        friendService.requestFriend(memberSeq, friendReqDto);
        return ResponseEntity.ok(ResponseDto.ok("친구 요청을 보냈습니다.", HttpStatus.OK));
    }

    // 2. 친구 요청 수락
    @PostMapping("/accept/{friendSeq}")
    public ResponseEntity<ResponseDto<?>> acceptFriendRequest(
            @RequestHeader("X-Member-Seq") Long memberSeq,
            @PathVariable Long friendSeq) {
        friendService.acceptFriendRequest(friendSeq, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok("친구 요청을 수락했습니다.", HttpStatus.OK));
    }


    // 3. 친구 요청 거절
    @DeleteMapping("/reject/{friendSeq}")
    public ResponseEntity<ResponseDto<?>> rejectFriendRequest(
            @RequestHeader("X-Member-Seq") Long memberSeq,
            @PathVariable Long friendSeq) {
        friendService.rejectFriendRequest(friendSeq, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok("친구 요청을 거절했습니다.", HttpStatus.OK));
    }


    // 4. 친구 목록 조회 및 검색
    @GetMapping("/list")
    public ResponseEntity<ResponseDto<?>> getFriendList(
            @RequestHeader("X-Member-Seq") Long memberSeq,
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        Page<FriendResDto> friendList = friendService.getFriendList(memberSeq, pageable, keyword);
        return ResponseEntity.ok(ResponseDto.ok(friendList, HttpStatus.OK));
    }


    // 5. 보낸 요청 목록 조회
    @GetMapping("/sent")
    public ResponseEntity<ResponseDto<?>> getSentRequestList(
            @RequestHeader("X-Member-Seq") Long memberSeq,
            Pageable pageable) {
        Page<FriendResDto> sentRequestList = friendService.getSentRequestList(memberSeq, pageable);
        return ResponseEntity.ok(ResponseDto.ok(sentRequestList, HttpStatus.OK));
    }

    // 6. 받은 요청 목록 조회
    @GetMapping("/received")
    public ResponseEntity<ResponseDto<?>> getReceivedRequestList(
            @RequestHeader("X-Member-Seq") Long memberSeq,
            Pageable pageable) {
        Page<ReceivedReqDto> receivedRequestList = friendService.getReceivedRequestList(memberSeq, pageable);
        return ResponseEntity.ok(ResponseDto.ok(receivedRequestList, HttpStatus.OK));
    }

    // 7. 친구 삭제 (친구 끊기)
    @DeleteMapping("/{friendMemberSeq}")
    public ResponseEntity<ResponseDto<?>> deleteFriend(
            @RequestHeader("X-Member-Seq") Long memberSeq,
            @PathVariable Long friendMemberSeq) {
        friendService.deleteFriend(friendMemberSeq, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok("친구를 삭제했습니다.", HttpStatus.OK));
    }
}
