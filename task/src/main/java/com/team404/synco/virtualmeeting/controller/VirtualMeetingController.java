package com.team404.synco.virtualmeeting.controller;

import com.team404.synco.common.constant.dto.DelegateSuperAuthorityReqDto;
import com.team404.synco.common.constant.dto.ResponseDto;
import com.team404.synco.virtualmeeting.dto.*;
import com.team404.synco.virtualmeeting.service.VirtualMeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/virtual-meeting")
public class VirtualMeetingController {
    private final VirtualMeetingService virtualMeetingService;

    // 기본 채널 생성
    @PostMapping("/createBasicChannel")
    public ResponseEntity<ResponseDto<?>> createBasicChannel(@RequestBody ChannelCreateReqDto channelCreateReqDto){
        Long id = virtualMeetingService.createBasicChannel(channelCreateReqDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.ok(id, HttpStatus.CREATED));
    }

    // 채널 생성
    @PostMapping("/createChannel")
    public ResponseEntity<ResponseDto<?>> createChannel(@RequestBody ChannelCreateReqDto channelCreateReqDto,
                                                        @RequestHeader("X-Member-Seq") Long memberSeq) throws AccessDeniedException {
        ChannelCreateResDto channelCreateResDto = virtualMeetingService.createChannel(channelCreateReqDto, memberSeq);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.ok(channelCreateResDto, HttpStatus.CREATED));
    }

    // 채널 수정
    @PatchMapping("/rename")
    public ResponseEntity<ResponseDto<?>> renameChannel(@RequestBody ChannelEditReqDto channelEditReqDto,
                                                        @RequestHeader("X-Member-Seq") Long memberSeq) throws AccessDeniedException {
        ChannelEditResDto channelEditResDto = virtualMeetingService.renameChannel(channelEditReqDto, memberSeq);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.ok(channelEditResDto, HttpStatus.CREATED));
    }

    // 채널 삭제
    @DeleteMapping("/{channelSeq}")
    public ResponseEntity<ResponseDto<?>> deleteChannel(@PathVariable("channelSeq") Long channelSeq,
                                                        @RequestHeader("X-Member-Seq") Long memberSeq) throws AccessDeniedException {
        virtualMeetingService.deleteChannel(channelSeq, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok("채널이 삭제되었습니다.", HttpStatus.OK));
    }

    // 채널에 멤버 추가
    @PostMapping("/addMember")
    public ResponseEntity<ResponseDto<?>> addMember(@RequestBody ChannelInviteReqDto channelInviteReqDto,
                                                    @RequestHeader("X-Member-Seq") Long memberSeq) throws AccessDeniedException {
        Long id = virtualMeetingService.addMemberToChannel(channelInviteReqDto, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok(id, HttpStatus.OK));
    }

    // 채널 권한 설정
    @PostMapping("/changeChannelAuthority")
    public ResponseEntity<ResponseDto<?>> changeChannelAuthority(@RequestBody GrantAuthorityReqDto grantAuthorityReqDto,
                                                                 @RequestHeader("X-Member-Seq") Long memberSeq) throws AccessDeniedException {
        ChannelGrantResDto channelGrantResDto = virtualMeetingService.grantToMember(grantAuthorityReqDto, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok(channelGrantResDto, HttpStatus.OK));
    }

    // 채널 SUPER 권한 위임
    @PostMapping("/delegateSuperAuthority")
    public ResponseEntity<ResponseDto<?>> delegateSuperAuthority(@RequestBody DelegateSuperAuthorityReqDto delegateSuperAuthorityReqDto,
                                                                 @RequestHeader("X-Member-Seq") Long memberSeq) throws AccessDeniedException
    {
        virtualMeetingService.delegateSuperAuthority(delegateSuperAuthorityReqDto, memberSeq);
        return ResponseEntity.ok(ResponseDto.ok("채널의 SUPER 권한 사용자가 변경되었습니다.", HttpStatus.OK));
    }

    // 전체 채널 삭제(워크스페이스 삭제시)
    @DeleteMapping("/{workSpaceSeq}")
    public void deleteAllChannel(@PathVariable Long workSpaceSeq){
        virtualMeetingService.deleteAllChannel(workSpaceSeq);
    }
}
