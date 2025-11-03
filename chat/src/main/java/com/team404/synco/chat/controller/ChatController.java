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
    public ResponseEntity<?> uploadFiles(
            @PathVariable Long channelSeq,
            @RequestPart("files") List<MultipartFile> files) {
        try {
            List<String> urls = chatService.uploadChatFiles(channelSeq, files); // S3 업로드 + URL 반환
            return ResponseEntity.ok(Map.of("uploadedUrls", urls));
        } catch (IllegalArgumentException e) {
            // 파일 개수 제한 초과 시
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // 기타 오류
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "파일 업로드 중 오류가 발생했습니다."));
        }
    }

    // 채팅 참여자 목록 조회 (1:1 사용자정보 조회)
    @GetMapping("/channels/{channelSeq}/members")
    public ResponseEntity<ResponseDto<?>> getChannelMembers(
            @PathVariable Long channelSeq,
            @RequestHeader("X-Member-Seq") Long memberSeq) throws AccessDeniedException {
        List<IndividualChatUserResDto> members = chatService.getChannelMembers(channelSeq, memberSeq);
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

     // 이전 메시지 조회 (무한스크롤)
     @GetMapping("/channels/{channelSeq}/messages")
     public ResponseEntity<?> loadMoreMessages(
             @PathVariable Long channelSeq,
             @RequestParam(required = false) Long lastId) {
         return ResponseEntity.ok(chatService.loadMoreMessages(channelSeq, lastId));
     }

     // 마지막 읽은 이후 메시지 조회
     @GetMapping("/channels/{channelSeq}/messages/after-last-read")
     public ResponseEntity<?> getMessagesAfterLastRead(
             @PathVariable Long channelSeq,
             @RequestHeader("X-Member-Seq") Long memberSeq) {

         return ResponseEntity.ok(chatService.getMessagesAfterLastRead(channelSeq, memberSeq));
     }

     // 마지막 읽은 메시지 업데이트
     @PostMapping("/channels/{channelSeq}/read")
     public ResponseEntity<?> updateLastRead(
             @PathVariable Long channelSeq,
             @RequestHeader("X-Member-Seq") Long memberSeq) {

         chatService.updateLastRead(channelSeq, memberSeq);
         return ResponseEntity.ok(ResponseDto.ok("UPDATED", HttpStatus.OK));
     }

    // 1:1 채팅채널 생성 또는 기존 channelSeq 반환
    @PostMapping("/channels/individual")
    public ResponseEntity<ResponseDto<?>> createOrGetIndividualChatChannel (
            @RequestBody IndividualChatCreateReqDto dto,
            @RequestHeader("X-Member-Seq") Long memberSeq) {

        Long channelSeq = chatService.createOrGetIndividualChatChannel(dto.getWorkSpaceSeq(), memberSeq, dto.getOtherMemberSeq());
        return ResponseEntity.ok(ResponseDto.ok(channelSeq, HttpStatus.OK));
    }

    // 1:1 채팅목록 조회
    @GetMapping("/channels/individual")
    public ResponseEntity<ResponseDto<?>> getIndividualChatChannels(
            @RequestHeader("X-Member-Seq") Long memberSeq) {
        log.info("📥 개인 채팅목록 요청: memberSeq={}", memberSeq);
        List<MyChatListResDto> myChatListResDtos = chatService.getIndividualChatChannels(memberSeq, WorkSpaceType.INDIVIDUAL);
        return ResponseEntity.ok(ResponseDto.ok(myChatListResDtos, HttpStatus.OK));
    }

    // 1:1 채팅채널 나가기
    @DeleteMapping("/channels/{channelSeq}/leave")
    public ResponseEntity<ResponseDto<?>> leaveIndividualChatChannel(
            @PathVariable Long channelSeq,
            @RequestHeader("X-Member-Seq") Long memberSeq) throws AccessDeniedException {

        log.info("🚪 채널 나가기 요청: channelSeq={}, memberSeq={}", channelSeq, memberSeq);
        chatService.leaveIndividualChatChannel(channelSeq, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok("채널에서 나갔습니다.", HttpStatus.OK));
    }
}
