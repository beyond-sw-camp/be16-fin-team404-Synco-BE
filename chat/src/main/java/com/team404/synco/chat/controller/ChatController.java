package com.team404.synco.chat.controller;

import com.team404.synco.chat.dto.*;
import com.team404.synco.chat.entity.WorkSpaceType;
import com.team404.synco.chat.service.ChatService;
import com.team404.synco.common.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;


    // 채팅서버 테스트
    @GetMapping("/test")
    public String test() {
        return "OK";
    }

    // 채널 생성
    @PostMapping("/createChannel")
    public ResponseEntity<ResponseDto<?>> createChannel(@RequestBody ChannelCreateReqDto channelCreateReqDto) {
        Long id = chatService.createChannel(channelCreateReqDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.ok(id, HttpStatus.CREATED));
    }

    // 채널에 멤버 추가
    @PostMapping("/addMember")
    public ResponseEntity<ResponseDto<?>> addMember(@RequestBody ChannelInviteReqDto channelInviteReqDto){
        Long id = chatService.addMemberToChannel(channelInviteReqDto);
        return ResponseEntity.ok(ResponseDto.ok(id, HttpStatus.OK));
    }

    // 채팅목록 조회 (개인워크스페이스)
    @GetMapping("/channels/personal")
    public ResponseEntity<List<MyChatListResDto>> getPersonalChatChannels(@RequestHeader("X-Member-Seq") Long memberSeq) {
        List<MyChatListResDto> result = chatService.getMyChatChannelsByWorkspace(memberSeq, WorkSpaceType.INDIVIDUAL);
        return ResponseEntity.ok(result);
    }

    // 채팅목록 조회 (프로젝트워크스페이스)
    @GetMapping("/channels/project/{workspaceSeq}")
    public ResponseEntity<List<MyChatListResDto>> getProjectChatChannels(@RequestHeader("X-Member-Seq") Long memberSeq,
                                                                         @PathVariable Long workspaceSeq) {
        List<MyChatListResDto> result = chatService.getMyChatChannelsByProjectWorkspace(memberSeq, workspaceSeq);
        return ResponseEntity.ok(result);
    }

    // 

    // 채팅 메시지 삭제 (hard-delete)


//
//    // 내 채팅목록 조회
//    @GetMapping("/my/channels")
//    public ResponseEntity<?> getMyChannels() {
//        List<MyChatChannelListDto> MychatChannelListDtos = chatService.getMyChannels();
//        return new ResponseEntity<>(MychatChannelListDtos, HttpStatus.OK);
//    }
//
//    // 이전 메시지 조회
//    @GetMapping("history/{channelSeq}")
//    public ResponseEntity<?> getChatHistory(@PathVariable Long channelSeq) {
//        List<ChatMessageDto> chatMessageDtos = chatService.getChatHistory(channelSeq);
//        return new ResponseEntity<>(chatMessageDtos, HttpStatus.OK);
//    }
//
//    // 채팅메시지 읽음처리
//    @PostMapping("/room/{channelSeq}/read")
//    public ResponseEntity messageRead(@PathVariable Long channelSeq) {
//        chatService.messageRead(channelSeq);
//        return ResponseEntity.ok().build();
//    }
//
//    // 채널 나가기
//    @DeleteMapping("/room/group/{channelSeq}/leave")
//    public ResponseEntity<?> leaveGroupChatRoom(@PathVariable Long channelSeq) {
//        chatService.leaveGroupChatRoom(channelSeq);
//        return ResponseEntity.ok().build();
//    }

//    // 1:1채팅방 개설 또는 기존 channelSeq return
//    @PostMapping("/room/private/create")
//    public ResponseEntity<?> getOrCreatePrivateRoom(@RequestParam Long otherMemberId) {
//        Long roomId = chatService.getOrCreatePrivateRoom(otherMemberId);
//        return new ResponseEntity<>(roomId, HttpStatus.OK);
//    }
//
}
