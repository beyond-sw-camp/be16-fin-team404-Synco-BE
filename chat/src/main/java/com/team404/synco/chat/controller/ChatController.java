package com.team404.synco.chat.controller;

import com.team404.synco.chat.dto.*;
import com.team404.synco.chat.dto.channel.*;
import com.team404.synco.chat.entity.WorkSpaceType;
import com.team404.synco.chat.service.ChatService;
import com.team404.synco.common.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
@Slf4j
public class ChatController {
    private final ChatService chatService;

    // 기본 채널 생성
    @PostMapping("/createBasicChannel")
    public ResponseEntity<ResponseDto<?>> createBasicChannel(@RequestBody ChannelCreateReqDto channelCreateReqDto) {
        Long id = chatService.createBasicChannel(channelCreateReqDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.ok(id, HttpStatus.CREATED));
    }

    // 채널 생성
    @PostMapping("/createChannel")
    public ResponseEntity<ResponseDto<?>> createChannel(@RequestBody ChannelCreateReqDto channelCreateReqDto,
            @RequestHeader("X-Member-Seq") Long memberSeq) throws AccessDeniedException {
        ChannelCreateResDto channelCreateResDto = chatService.createChannel(channelCreateReqDto, memberSeq);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.ok(channelCreateResDto, HttpStatus.CREATED));
    }

    // 채널 수정
    @PatchMapping("/rename")
    public ResponseEntity<ResponseDto<?>> renameChannel(@RequestBody ChannelEditReqDto channelEditReqDto,
            @RequestHeader("X-Member-Seq") Long memberSeq) throws AccessDeniedException {
        ChannelEditResDto channelEditResDto = chatService.renameChannel(channelEditReqDto, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok(channelEditResDto, HttpStatus.OK));
    }

    // 채널 삭제
    @DeleteMapping("/channel/{channelSeq}")
    public ResponseEntity<ResponseDto<?>> deleteChannel(@PathVariable("channelSeq") Long channelSeq,
            @RequestHeader("X-Member-Seq") Long memberSeq) throws AccessDeniedException {
        chatService.deleteChannel(channelSeq, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok("채널이 삭제되었습니다.", HttpStatus.OK));
    }

    // 채널에 멤버 추가
    @PostMapping("/addMember")
    public ResponseEntity<ResponseDto<?>> addMember(@RequestBody ChannelInviteReqDto channelInviteReqDto,
            @RequestHeader("X-Member-Seq") Long memberSeq) throws AccessDeniedException {
        Long id = chatService.addMemberToChannel(channelInviteReqDto, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok(id, HttpStatus.OK));
    }

    // 채널 권한 설정
    @PatchMapping("/changeChannelAuthority")
    public ResponseEntity<ResponseDto<?>> changeChannelAuthority(@RequestBody GrantAuthorityReqDto grantAuthorityReqDto,
            @RequestHeader("X-Member-Seq") Long memberSeq) throws AccessDeniedException {
        ChannelGrantResDto channelGrantResDto = chatService.grantToMember(grantAuthorityReqDto, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok(channelGrantResDto, HttpStatus.OK));
    }

    // 채널 SUPER 권한 위임
    @PostMapping("/delegateSuperAuthority")
    public ResponseEntity<ResponseDto<?>> delegateSuperAuthority(
            @RequestBody DelegateSuperAuthorityReqDto delegateSuperAuthorityReqDto,
            @RequestHeader("X-Member-Seq") Long memberSeq) throws AccessDeniedException {
        chatService.delegateSuperAuthority(delegateSuperAuthorityReqDto, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok("채널의 SUPER 권한 사용자가 변경되었습니다.", HttpStatus.OK));
    }

    // 채널 리스트 조회
    @GetMapping("/channels/{workSpaceSeq}")
    public ResponseEntity<ResponseDto<?>> getChannelList(@PathVariable("workSpaceSeq") Long workSpaceSeq)
    {
        return ResponseEntity.ok(ResponseDto.ok(chatService.findChatChannelList(workSpaceSeq), HttpStatus.OK));
    }

    // 전체 채널 삭제(프로젝트 삭제시)
    @DeleteMapping("/{workSpaceSeq}")
    public ResponseEntity<ResponseDto<?>> deleteAllChannel(@PathVariable("workSpaceSeq") Long workSpaceSeq) {
        chatService.deleteAllChannel(workSpaceSeq);
        return ResponseEntity.ok(ResponseDto.ok("삭제 완료", HttpStatus.OK));
    }

    // 프로젝트 탈퇴
    @DeleteMapping("/leave/{workSpaceSeq}")
    public void leaveWorkSpace(@PathVariable("workSpaceSeq") Long workSpaceSeq,
            @RequestHeader("X-Member-Seq") Long memberSeq) {
        chatService.deleteMemberFromWorkSpace(workSpaceSeq, memberSeq);
    }

    // 프로젝트 강제탈퇴
    @DeleteMapping("/kick")
    public void kickFromWorkSpace(@RequestBody KickMemberFromWorkSpaceReqDto kickMemberFromWorkSpaceReqDto) {
        Long workSpaceSeq = kickMemberFromWorkSpaceReqDto.getWorkSpaceSeq();
        Long memberSeq = kickMemberFromWorkSpaceReqDto.getMemberSeq();
        chatService.deleteMemberFromWorkSpace(workSpaceSeq, memberSeq);
    }

    /////////////////////////////////////////// 채팅기능////////////////////////////////////////////////
    // 첨부파일 업로드
    @PostMapping("/files/upload/{channelSeq}")
    public ResponseEntity<Map<String, List<String>>> uploadFiles(
            @PathVariable Long channelSeq,
            @RequestPart("files") List<MultipartFile> files) {
        List<String> urls = chatService.uploadChatFiles(channelSeq, files); // S3 업로드 + URL 반환
        return ResponseEntity.ok(Map.of("uploadedUrls", urls));
    }

    // 채팅 참여자 목록 조회
    @GetMapping("/channels/{channelSeq}/members")
    public ResponseEntity<ResponseDto<?>> getChannelMembers(
            @PathVariable Long channelSeq,
            @RequestHeader("X-Member-Seq") Long memberSeq) throws AccessDeniedException {
        List<ChannelMemberResDto> members = chatService.getChannelMembers(channelSeq, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok(members, HttpStatus.OK));
    }

    // 채팅 메시지 삭제 (hard-delete)
    @DeleteMapping("/messages/{chatMessageSeq}")
    public ResponseEntity<ResponseDto<?>> deleteChatMessage(
            @PathVariable Long chatMessageSeq,
            @RequestHeader("X-Member-Seq") Long memberSeq) throws AccessDeniedException {

        chatService.deleteChatMessage(chatMessageSeq, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok("메시지가 영구 삭제되었습니다.", HttpStatus.OK));
    }

    // 채팅목록 조회 (개인워크스페이스)
    @GetMapping("/channels/personal")
    public ResponseEntity<List<MyChatListResDto>> getPersonalChatChannels(
            @RequestHeader("X-Member-Seq") Long memberSeq) {
        List<MyChatListResDto> result = chatService.getMyChatChannelsByWorkspace(memberSeq, WorkSpaceType.INDIVIDUAL);
        return ResponseEntity.ok(result);
    }

     // 이전 메시지 조회
     @GetMapping("history/{channelSeq}")
     public ResponseEntity<ResponseDto<?>> getChatHistory(
             @PathVariable Long channelSeq,
             @RequestHeader("X-Member-Seq") Long memberSeq) {
     List<ChatMessageDto> chatMessageDtos =
     chatService.getChatHistory(channelSeq);
     return ResponseEntity.ok(ResponseDto.ok("읽음 처리 완료 "));
     }
    @GetMapping("/channels/{channelSeq}/unread-count")
    public ResponseEntity<ResponseDto<?>> getUnreadCount(
            @PathVariable Long channelSeq,
            @RequestHeader("X-Member-Seq") Long memberSeq) {

        int unreadCount = chatService.getUnreadCount(channelSeq, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok(unreadCount, HttpStatus.OK));
    }

     // 채팅메시지 읽음처리
     @PostMapping("/channel/{channelSeq}/read")
     public ResponseEntity<ResponseDto<?>> markMessagesAsRead(
             @PathVariable Long channelSeq,
             @RequestHeader("X-Member-Seq") Long memberSeq) {

         chatService.markMessagesAsRead(channelSeq, memberSeq);
         return ResponseEntity.ok(ResponseDto.ok("읽음 처리 완료", HttpStatus.OK));
     }

    // 1:1 채팅목록 조회
    // @GetMapping("/my/channels")
    // public ResponseEntity<?> getMyChannels() {
    // List<MyChatChannelListDto> MychatChannelListDtos =
    // chatService.getMyChannels();
    // return new ResponseEntity<>(MychatChannelListDtos, HttpStatus.OK);
    // }
    //
    // 1:1 채널 나가기
    // @DeleteMapping("/room/group/{channelSeq}/leave")
    // public ResponseEntity<?> leaveGroupChatRoom(@PathVariable Long channelSeq) {
    // chatService.leaveGroupChatRoom(channelSeq);
    // return ResponseEntity.ok().build();
    // }
    // // 1:1 채팅방 개설 또는 기존 channelSeq return
    // @PostMapping("/room/private/create")
    // public ResponseEntity<?> getOrCreatePrivateRoom(@RequestParam Long
    // otherMemberId) {
    // Long roomId = chatService.getOrCreatePrivateRoom(otherMemberId);
    // return new ResponseEntity<>(roomId, HttpStatus.OK);
    // }
    //
}
