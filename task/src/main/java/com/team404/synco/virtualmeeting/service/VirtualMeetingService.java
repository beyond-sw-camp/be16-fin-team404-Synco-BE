package com.team404.synco.virtualmeeting.service;

import com.team404.synco.common.constant.Authority;
import com.team404.synco.virtualmeeting.dto.ChannelCreateReqDto;
import com.team404.synco.virtualmeeting.dto.ChannelInviteReqDto;
import com.team404.synco.virtualmeeting.entity.VirtualMeetingChannel;
import com.team404.synco.virtualmeeting.entity.VirtualMeetingChannelMember;
import com.team404.synco.virtualmeeting.repository.VirtualMeetingChannelMemberRepository;
import com.team404.synco.virtualmeeting.repository.VirtualMeetingChannelRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@Slf4j
public class VirtualMeetingService {
    private final VirtualMeetingChannelRepository virtualMeetingChannelRepository;
    private final VirtualMeetingChannelMemberRepository virtualMeetingChannelMemberRepository;

    public VirtualMeetingService(VirtualMeetingChannelRepository virtualMeetingChannelRepository,
                                 VirtualMeetingChannelMemberRepository virtualMeetingChannelMemberRepository) {
        this.virtualMeetingChannelRepository = virtualMeetingChannelRepository;
        this.virtualMeetingChannelMemberRepository = virtualMeetingChannelMemberRepository;
    }

    // 채널 생성
    // ToDO : 우선은 기본 채널 생성만 작업했습니다. 추후 채널 추가 가능하도록 코드 수정 예정입니다.
    public Long createChannel(ChannelCreateReqDto channelCreateReqDto){
        VirtualMeetingChannel virtualMeetingChannel = virtualMeetingChannelRepository.save(channelCreateReqDto.toEntity());

        VirtualMeetingChannelMember virtualMeetingChannelMember = VirtualMeetingChannelMember.builder()
                .memberSeq(channelCreateReqDto.getMemberSeq())
                .authority(Authority.SUPER)
                .virtualMeetingChannel(virtualMeetingChannel)
                .build();

        // 초대된 멤버 추가
        List<Long> friendList = channelCreateReqDto.getFriendList();
        if(friendList != null && !friendList.isEmpty()){
            for(Long memberSeq : friendList){
                VirtualMeetingChannelMember virtualMeetingChannelFriendList = VirtualMeetingChannelMember.builder()
                        .memberSeq(memberSeq)
                        .authority(Authority.PARTICIPANT)
                        .virtualMeetingChannel(virtualMeetingChannel)
                        .build();
                virtualMeetingChannelMemberRepository.save(virtualMeetingChannelFriendList);
            }
        }

        virtualMeetingChannelMemberRepository.save(virtualMeetingChannelMember);
        return virtualMeetingChannel.getVirtualMeetingChannelSeq();
    }

    // 멤버 추가
    public Long addMemberToChannel(ChannelInviteReqDto channelInviteReqDto){
        VirtualMeetingChannel virtualMeetingChannel = virtualMeetingChannelRepository.findByVirtualMeetingChannelSeqAndWorkSpaceSeq(
                channelInviteReqDto.getChannelSeq(), channelInviteReqDto.getWorkSpaceSeq()).orElseThrow(() ->
                new EntityNotFoundException("등록되지 않은 채널입니다."));
        log.info(virtualMeetingChannel.toString());
        List<Long> friendList = channelInviteReqDto.getFriendList();
        for(Long memberSeq : friendList){
            VirtualMeetingChannelMember virtualMeetingChannelMember = VirtualMeetingChannelMember.builder()
                    .memberSeq(memberSeq)
                    .authority(Authority.PARTICIPANT)
                    .virtualMeetingChannel(virtualMeetingChannel)
                    .build();
            virtualMeetingChannelMemberRepository.save(virtualMeetingChannelMember);
        }
        return (long) channelInviteReqDto.getFriendList().size();

    }

    // 채널 전체 삭제(WorkSpace 삭제시)
    public void deleteAllChannel(Long workSpaceSeq){
        virtualMeetingChannelRepository.deleteAllByWorkSpaceSeq(workSpaceSeq);
    }
}
