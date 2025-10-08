package com.team404.synco.workspace.service;

import com.team404.synco.common.constant.WorkSpaceType;
import com.team404.synco.common.service.S3Uploader;
import com.team404.synco.member.entity.Member;
import com.team404.synco.member.repository.MemberRepository;
import com.team404.synco.workspace.dto.*;
import com.team404.synco.workspace.entity.WorkSpace;
import com.team404.synco.workspace.repository.WorkSpaceRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Transactional
@Slf4j
public class WorkSpaceService {
    private final WorkSpaceRepository workSpaceRepository;
    private final MemberRepository memberRepository;
    private final WorkSpaceRedisService workSpaceRedisService;
    private final S3Uploader s3Uploader;
    private final ChatFeign chatFeign;
    private final DriveFeign driveFeign;
    private final TaskFeign taskFeign;

    public WorkSpaceService(WorkSpaceRepository workSpaceRepository, MemberRepository memberRepository, WorkSpaceRedisService workSpaceRedisService, S3Uploader s3Uploader, ChatFeign chatFeign, DriveFeign driveFeign, TaskFeign taskFeign) {
        this.workSpaceRepository = workSpaceRepository;
        this.memberRepository = memberRepository;
        this.workSpaceRedisService = workSpaceRedisService;
        this.s3Uploader = s3Uploader;
        this.chatFeign = chatFeign;
        this.driveFeign = driveFeign;
        this.taskFeign = taskFeign;
    }

    // 개인 워크스페이스 생성
    public WorkSpaceResDto createIndividualWorkSpace(Long memberSeq) {
        // 멤버 불러오기
        Member member = memberRepository.findById(memberSeq).orElseThrow(() -> new EntityNotFoundException("없는 회원입니다."));

        // 워크스페이스 생성
        WorkSpace workSpace = workSpaceRepository.save(WorkSpace.builder()
                .member(member)
                .workSpaceName(member.getName())
                .workSpaceThumbnailImageUrl(member.getProfileImageUrl())
                .workSpaceType(WorkSpaceType.INDIVIDUAL)
                .build());

        // 개인 드라이브 생성
        driveFeign.createPersonalDrive(
                DriveCreateReqDto.builder()
                        .workSpaceType(WorkSpaceType.INDIVIDUAL)
                        .workSpaceName(workSpace.getWorkSpaceName())
                        .workSpaceReq(workSpace.getWorkSpaceSeq())
                        .build()
        );

        // 개인 일정 생성
        taskFeign.createTask(
                TaskChannelMemberCreateReqDto.builder()
                        .memberSeq(workSpace.getMember().getMemberSeq())
                        .workSpaceReq(workSpace.getWorkSpaceSeq())
                        .build()
        );

        return WorkSpaceResDto.fromEntity(workSpace);
    }

    // 팀 워크스페이스 생성
    public WorkSpaceResDto createTeamWorkSpace(TeamWorkSpaceCreateReqDto teamWorkSpaceCreateReqDto, Long memberSeq) {
        // 워크스페이스 생성한 멤버 정보 불러오기
        Member member = memberRepository.findById(memberSeq).orElseThrow(() -> new EntityNotFoundException("없는 회원입니다."));
        // 워크스페이스 썸네일 이미지 업로드
        String workSpaceThumbnailImageUrl = addWorkSpaceThumbnailImageUrl(teamWorkSpaceCreateReqDto.getWorkSpaceThumbnailImage());
        // 워크스페이스 생성
        WorkSpace workSpace = workSpaceRepository.save(WorkSpace.builder()
                .member(member)
                .workSpaceName(teamWorkSpaceCreateReqDto.getWorkSpaceName())
                .workSpaceThumbnailImageUrl(workSpaceThumbnailImageUrl)
                .workSpaceType(WorkSpaceType.TEAM)
                .build());

        // 워크스페이스 생성한 member정보 redis에 저장
        workSpaceRedisService.addMemberInfo(member);
        workSpaceRedisService.addWorkSpace(workSpace, member.getMemberSeq());
        workSpaceRedisService.addMemberToWorkSpace(workSpace, member.getMemberSeq());

        // 워크스페이스에 초대된 member정보 redis에 저장
        List<Long> inviteMemberList = teamWorkSpaceCreateReqDto.getMemberList();
        for(Long inviteMemberSeq : inviteMemberList){
            Member inviteMember = memberRepository.findById(inviteMemberSeq).orElseThrow(() -> new EntityNotFoundException("없는 회원입니다."));
            workSpaceRedisService.addMemberInfo(inviteMember);
            workSpaceRedisService.addWorkSpace(workSpace, inviteMemberSeq);
            workSpaceRedisService.addMemberToWorkSpace(workSpace, inviteMemberSeq);
        }


        // 기본 채팅 채널 생성
        chatFeign.createChatChannel(
                ChannelCreateReqDto.builder()
                        .ChannelName("일반")
                        .workSpaceSeq(workSpace.getWorkSpaceSeq())
                        .memberSeq(workSpace.getMember().getMemberSeq())
                        .memberList(teamWorkSpaceCreateReqDto.getMemberList())
                        .build()
        );

        // 기본 드라이브 생성
        driveFeign.createTeamDrive(
                DriveCreateReqDto.builder()
                        .workSpaceType(WorkSpaceType.TEAM)
                        .workSpaceName(workSpace.getWorkSpaceName())
                        .workSpaceReq(workSpace.getWorkSpaceSeq())
                        .build()
        );

        // 기본 화상회의 채널 생성
        taskFeign.createVirtualMeetChannel(
                ChannelCreateReqDto.builder()
                        .ChannelName("일반")
                        .workSpaceSeq(workSpace.getWorkSpaceSeq())
                        .memberSeq(workSpace.getMember().getMemberSeq())
                        .memberList(teamWorkSpaceCreateReqDto.getMemberList())
                        .build()
        );

        // 기본 task 생성
        taskFeign.createTask(
                TaskChannelMemberCreateReqDto.builder()
                        .memberSeq(memberSeq)
                        .workSpaceReq(workSpace.getWorkSpaceSeq())
                        .memberList(teamWorkSpaceCreateReqDto.getMemberList())
                        .build()
        );
        return WorkSpaceResDto.fromEntity(workSpace);
    }

    // 각 채널에 멤버 초대
    public void channelInvite(ChannelInviteReqDto channelInviteReqDto) {
        chatFeign.addMemberToChannel(channelInviteReqDto);
        taskFeign.addMemberToTaskChannel(channelInviteReqDto);
        taskFeign.addMemberToVirtualMeetingChannel(channelInviteReqDto);
    }

//    // 내 워크스페이스 목록 조회
//    public Page<WorkSpaceListResDto> myWorkSpaceList(){
//        return null;
//    }

//    // 워크스페이스 상세 조회(대시보드)
//    public WorkSpaceDetailResDto workSpaceDetail(){
//        return null;
//    }
//
//    // 워크스페이스 수정
//    public WorkSpaceEditResDto editWorkSpace(){
//        return null;
//    }
//
//    // 워크스페이스 삭제
//    public Long deleteWorkSpace(){
//        return null;
//    }
//

    // 워크스페이스 프로필 이미지 삽입
    private String addWorkSpaceThumbnailImageUrl(MultipartFile workSpaceThumbnailImage) {
        String workSpaceThumbnailImageUrl = null;
        if (workSpaceThumbnailImage != null && !workSpaceThumbnailImage.isEmpty()) {
            workSpaceThumbnailImageUrl = s3Uploader.upload(workSpaceThumbnailImage, "workSpaceThumbnail");
        }
        return workSpaceThumbnailImageUrl;
    }
}
