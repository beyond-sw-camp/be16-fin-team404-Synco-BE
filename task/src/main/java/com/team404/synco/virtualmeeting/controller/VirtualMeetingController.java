package com.team404.synco.virtualmeeting.controller;

import com.team404.synco.common.constant.dto.DelegateSuperAuthorityReqDto;
import com.team404.synco.common.constant.dto.ResponseDto;
import com.team404.synco.virtualmeeting.dto.Feign.*;
import com.team404.synco.virtualmeeting.dto.MemberInfoDto;
import com.team404.synco.virtualmeeting.dto.Room.RoomActiveListDto;
import com.team404.synco.virtualmeeting.dto.Room.RoomDetailDto;
import com.team404.synco.virtualmeeting.dto.Room.RoomEndedListDto;
import com.team404.synco.virtualmeeting.service.VirtualMeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/virtual-meeting")
public class VirtualMeetingController {
    private final VirtualMeetingService virtualMeetingService;

    // ====================================Feign 관련 메서드========================================

    // 기본 채널 생성
    @PostMapping("/createBasicChannel")
    public ResponseEntity<ResponseDto<?>> createBasicChannel(@RequestBody ChannelCreateReqDto channelCreateReqDto){
        Long id = virtualMeetingService.createBasicChannel(channelCreateReqDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.ok(id, HttpStatus.CREATED));
    }

    // 채널에 멤버 추가
    @PostMapping("/addMember")
    public ResponseEntity<ResponseDto<?>> addMember(@RequestBody ChannelInviteReqDto channelInviteReqDto,
                                                    @RequestHeader("X-Member-Seq") Long memberSeq) throws AccessDeniedException {
        Long id = virtualMeetingService.addMemberToChannel(channelInviteReqDto, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok(id, HttpStatus.OK));
    }

    // 채널 SUPER 권한 위임
    @PostMapping("/delegateSuperAuthority")
    public ResponseEntity<ResponseDto<?>> delegateSuperAuthority(@RequestBody DelegateSuperAuthorityReqDto delegateSuperAuthorityReqDto,
                                                                 @RequestHeader("X-Member-Seq") Long memberSeq) throws AccessDeniedException
    {
        virtualMeetingService.delegateSuperAuthority(delegateSuperAuthorityReqDto, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok("채널의 SUPER 권한 사용자가 변경되었습니다.", HttpStatus.OK));
    }

    // 채널 리스트 조회
    @GetMapping("/channels/{workSpaceSeq}")
    public ResponseEntity<ResponseDto<?>> getChannelList(@PathVariable("workSpaceSeq") Long workSpaceSeq)
    {
        return ResponseEntity.ok(ResponseDto.ok(virtualMeetingService.findChatChannelList(workSpaceSeq), HttpStatus.OK));
    }

    // 전체 채널 삭제(프로젝트 삭제시)
    @DeleteMapping("/{workSpaceSeq}")
    public void deleteAllChannel(@PathVariable("workSpaceSeq") Long workSpaceSeq){
        virtualMeetingService.deleteAllChannel(workSpaceSeq);
    }

    // 프로젝트 탈퇴
    @DeleteMapping("/leave/{workSpaceSeq}")
    public void leaveWorkSpace(@PathVariable("workSpaceSeq")Long workSpaceSeq, @RequestHeader("X-Member-Seq") Long memberSeq){
        virtualMeetingService.deleteMemberFromWorkSpace(workSpaceSeq, memberSeq);
    }

    // 프로젝트 강제탈퇴
    @DeleteMapping("/kick")
    public void kickFromWorkSpace(@RequestBody KickMemberFromWorkSpaceReqDto kickMemberFromWorkSpaceReqDto){
        Long workSpaceSeq = kickMemberFromWorkSpaceReqDto.getWorkSpaceSeq();
        Long memberSeq = kickMemberFromWorkSpaceReqDto.getMemberSeq();
        virtualMeetingService.deleteMemberFromWorkSpace(workSpaceSeq, memberSeq);
    }

    // =====================================일반 채널 관련 메서드=======================================

    // 채널 권한 설정
    @PatchMapping("/changeChannelAuthority")
    public ResponseEntity<ResponseDto<?>> changeChannelAuthority(@RequestBody GrantAuthorityReqDto grantAuthorityReqDto,
                                                                 @RequestHeader("X-Member-Seq") Long memberSeq) throws AccessDeniedException {
        ChannelGrantResDto channelGrantResDto = virtualMeetingService.grantToMember(grantAuthorityReqDto, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok(channelGrantResDto, HttpStatus.OK));
    }

    // 워크스페이스 멤버 목록 조회
    @GetMapping("/workspace/{workSpaceSeq}/members")
    public ResponseEntity<ResponseDto<?>> getWorkSpaceMemberList(@PathVariable("workSpaceSeq") Long workSpaceSeq) {
        List<MemberInfoDto> memberList = virtualMeetingService.getWorkSpaceMemberList(workSpaceSeq);
        return ResponseEntity.ok(ResponseDto.ok(memberList, HttpStatus.OK));
    }

    // =====================================LiveKit 룸 관련 메서드=======================================

    // 현재 진행중인 화상회의 목록
    @GetMapping("/channel/{channelSeq}/rooms/active")
    public ResponseEntity<ResponseDto<?>> getActiveRooms(
            @PathVariable Long channelSeq,
            @RequestHeader("X-Member-Seq") Long memberSeq,
            @PageableDefault(value = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<RoomActiveListDto> activeRooms = virtualMeetingService.getActiveRooms(channelSeq, memberSeq, pageable);
        return ResponseEntity.ok(ResponseDto.ok(activeRooms, HttpStatus.OK));
    }

    // 종료된 화상회의 룸 목록
    @GetMapping("/channel/{channelSeq}/rooms/ended")
    public ResponseEntity<ResponseDto<?>> getEndedRooms(
            @PathVariable Long channelSeq,
            @RequestHeader("X-Member-Seq") Long memberSeq,
            @PageableDefault(value = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<RoomEndedListDto> endedRooms = virtualMeetingService.getEndedRooms(channelSeq, memberSeq, pageable);
        return ResponseEntity.ok(ResponseDto.ok(endedRooms, HttpStatus.OK));
    }

    // 특정 화상회의 룸 요약 정보 조회
    @GetMapping("/rooms/{roomSeq}/summary")
    public ResponseEntity<ResponseDto<?>> getRoomSummaryDetail(
            @PathVariable Long roomSeq,
            @RequestHeader("X-Member-Seq") Long memberSeq) {
        RoomDetailDto roomDetail = virtualMeetingService.getRoomDetail(roomSeq, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok(roomDetail, HttpStatus.OK));
    }

    // 녹화 영상 다운로드 (S3에서 바이트 스트리밍)
    @GetMapping(value = "/rooms/{roomSeq}/recording/download")
    public ResponseEntity<byte[]> downloadRecording(
            @PathVariable Long roomSeq,
            @RequestHeader("X-Member-Seq") Long memberSeq) {
        return virtualMeetingService.downloadRecordingFile(roomSeq, memberSeq);
    }

}
