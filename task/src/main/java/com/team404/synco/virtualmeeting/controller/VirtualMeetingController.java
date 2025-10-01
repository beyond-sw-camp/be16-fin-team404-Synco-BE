package com.team404.synco.virtualmeeting.controller;

import com.team404.synco.common.constant.dto.ResponseDto;
import com.team404.synco.virtualmeeting.dto.VirtualMeetingChannelCreateReqDto;
import com.team404.synco.virtualmeeting.service.VirtualMeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/virtual-meeting")
public class VirtualMeetingController {
    private final VirtualMeetingService virtualMeetingService;
    @PostMapping("/create-channel")
    public ResponseEntity<?> createVirtualMeetChannel(@RequestBody VirtualMeetingChannelCreateReqDto virtualMeetingChannelCreateReqDto){
        Long id = virtualMeetingService.createChannel(virtualMeetingChannelCreateReqDto);
        return new ResponseEntity<>(ResponseDto.ok(id, HttpStatus.OK), HttpStatus.OK);
    };
}
