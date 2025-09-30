package com.team404.synco.virtualmeeting.service;

import com.team404.synco.common.constant.Authority;
import com.team404.synco.virtualmeeting.dto.VirtualMeetingChannelCreateReqDto;
import com.team404.synco.virtualmeeting.entity.VirtualMeetingChannel;
import com.team404.synco.virtualmeeting.entity.VirtualMeetingChannelMember;
import com.team404.synco.virtualmeeting.repository.VirtualMeetingChannelRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class VirtualMeetingService {
    private final VirtualMeetingChannelRepository virtualMeetingChannelRepository;

    public VirtualMeetingService(VirtualMeetingChannelRepository virtualMeetingChannelRepository) {
        this.virtualMeetingChannelRepository = virtualMeetingChannelRepository;
    }

    // 채널 생성
    // ToDO : 우선은 기본 채널 생성만 작업했습니다. 추후 채널 추가 가능하도록 코드 수정 예정입니다.
    public Long createChannel(VirtualMeetingChannelCreateReqDto virtualMeetingChannelCreateReqDto){
        VirtualMeetingChannel virtualMeetingChannel = VirtualMeetingChannel.builder()
                .virtualMeetingChannelName(virtualMeetingChannelCreateReqDto.getChatChannelName())
                .workSpaceSeq(virtualMeetingChannelCreateReqDto.getWorkSpaceSeq())
                .build();

        VirtualMeetingChannelMember virtualMeetingChannelMember = VirtualMeetingChannelMember.builder()
                .memberSeq(1L)
                .authority(Authority.SUPER)
                .virtualMeetingChannel(virtualMeetingChannel)
                .build();

        virtualMeetingChannel.getVirtualMeetingChannelMemberList().add(virtualMeetingChannelMember);
        return virtualMeetingChannelRepository.save(virtualMeetingChannel).getVirtualMeetingChannelSeq();
    }
}
