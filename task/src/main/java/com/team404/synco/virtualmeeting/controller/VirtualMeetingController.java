package com.team404.synco.virtualmeeting.controller;

import com.team404.synco.common.constant.dto.ResponseDto;
import com.team404.synco.virtualmeeting.dto.ChannelCreateReqDto;
import com.team404.synco.virtualmeeting.dto.ChannelInviteReqDto;
import com.team404.synco.virtualmeeting.service.VirtualMeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/virtual-meeting")
public class VirtualMeetingController {
    private final VirtualMeetingService virtualMeetingService;
    @PostMapping("/createChannel")
    public ResponseEntity<ResponseDto<?>> createVirtualMeetChannel(@RequestBody ChannelCreateReqDto channelCreateReqDto){
        Long id = virtualMeetingService.createChannel(channelCreateReqDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.ok(id, HttpStatus.CREATED));
    };

    // 채널에 멤버 추가
    @PostMapping("/addMember")
    public ResponseEntity<ResponseDto<?>> addMember(@RequestBody ChannelInviteReqDto channelInviteReqDto){
        Long id = virtualMeetingService.addMemberToChannel(channelInviteReqDto);
        return ResponseEntity.ok(ResponseDto.ok(id, HttpStatus.OK));
    }

    // 전체 채널 삭제(워크스페이스 삭제시)
    @DeleteMapping("/{workSpaceSeq}")
    public void deleteAllChannel(@PathVariable Long workSpaceSeq){
        virtualMeetingService.deleteAllChannel(workSpaceSeq);
    }
}
