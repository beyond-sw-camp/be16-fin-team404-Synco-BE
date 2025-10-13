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

import java.nio.file.AccessDeniedException;
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
    private static final String WORKSPACE_THUMBNAIL_DIRECTORY = "workspaceThumbnail";

    public WorkSpaceService(WorkSpaceRepository workSpaceRepository, MemberRepository memberRepository,
                            WorkSpaceRedisService workSpaceRedisService, S3Uploader s3Uploader,
                            ChatFeign chatFeign, DriveFeign driveFeign, TaskFeign taskFeign) {
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
        WorkSpace workSpace = workSpaceRepository.save(WorkSpace.builder().member(member).workSpaceName(member.getName()).
                workSpaceThumbnailImageUrl(member.getProfileImageUrl()).workSpaceType(WorkSpaceType.INDIVIDUAL).build());

        // 워크스페이스 생성한 member정보 redis에 저장
        workSpaceRedisService.addMemberInfo(member);
        workSpaceRedisService.addWorkSpace(workSpace, member.getMemberSeq());

        // 개인 드라이브 생성
        driveFeign.createPersonalDrive(DriveCreateReqDto.builder().workSpaceType(WorkSpaceType.INDIVIDUAL).workSpaceName(
                workSpace.getWorkSpaceName()).workSpaceReq(workSpace.getWorkSpaceSeq()).build());

        // 개인 일정 생성
        taskFeign.createTask(TaskChannelMemberCreateReqDto.builder().memberSeq(workSpace.getMember().getMemberSeq()).
                workSpaceReq(workSpace.getWorkSpaceSeq()).build());

        return WorkSpaceResDto.fromEntity(workSpace);
    }

    // 프로젝트 워크스페이스 생성
    public WorkSpaceResDto createTeamWorkSpace(TeamWorkSpaceCreateReqDto teamWorkSpaceCreateReqDto, Long memberSeq) {
        // 워크스페이스 생성한 멤버 정보 불러오기
        Member member = memberRepository.findById(memberSeq).orElseThrow(() -> new EntityNotFoundException("없는 회원입니다."));
        // 워크스페이스 썸네일 이미지 업로드
        String workSpaceThumbnailImageUrl = null;
        if (teamWorkSpaceCreateReqDto.getWorkSpaceThumbnailImage() != null &&
                !teamWorkSpaceCreateReqDto.getWorkSpaceThumbnailImage().isEmpty()) {
            workSpaceThumbnailImageUrl = s3Uploader.upload(teamWorkSpaceCreateReqDto.getWorkSpaceThumbnailImage()
                    , WORKSPACE_THUMBNAIL_DIRECTORY);
        }
        // 워크스페이스 생성
        WorkSpace workSpace = workSpaceRepository.save(WorkSpace.builder().member(member)
                .workSpaceName(teamWorkSpaceCreateReqDto.getWorkSpaceName()).workSpaceThumbnailImageUrl(workSpaceThumbnailImageUrl)
                .workSpaceType(WorkSpaceType.PROJECT).build());

        // 워크스페이스 생성한 member정보 redis에 저장
        workSpaceRedisService.addMemberInfo(member);
        workSpaceRedisService.addWorkSpace(workSpace, member.getMemberSeq());
        workSpaceRedisService.addMemberToWorkSpace(workSpace, member.getMemberSeq());

        // 워크스페이스에 초대된 member정보 redis에 저장
        List<Long> invitefriendList = teamWorkSpaceCreateReqDto.getFriendList();
        invitefriendList.stream().map(inviteMemberSeq -> memberRepository.findById(inviteMemberSeq)
                .orElseThrow(() -> new EntityNotFoundException("없는 회원입니다."))).forEach(inviteMember -> {
            workSpaceRedisService.addMemberInfo(inviteMember);
            workSpaceRedisService.addWorkSpace(workSpace, inviteMember.getMemberSeq());
            workSpaceRedisService.addMemberToWorkSpace(workSpace, inviteMember.getMemberSeq());
        });


        // 기본 채팅 채널 생성
        chatFeign.createChatBasicChannel(ChannelCreateReqDto.builder().channelName("일반").workSpaceSeq(workSpace.getWorkSpaceSeq())
                .memberSeq(workSpace.getMember().getMemberSeq()).friendList(teamWorkSpaceCreateReqDto.getFriendList()).build());

        // 기본 드라이브 생성
        driveFeign.createTeamDrive(DriveCreateReqDto.builder().workSpaceType(WorkSpaceType.PROJECT).workSpaceName(
                workSpace.getWorkSpaceName()).workSpaceReq(workSpace.getWorkSpaceSeq()).build());

        // 기본 화상회의 채널 생성
        taskFeign.createVirtualMeetBasicChannel(ChannelCreateReqDto.builder().channelName("일반").workSpaceSeq(
                        workSpace.getWorkSpaceSeq()).memberSeq(workSpace.getMember().getMemberSeq()).
                friendList(teamWorkSpaceCreateReqDto.getFriendList()).build());

        // 기본 task 생성
        taskFeign.createTask(TaskChannelMemberCreateReqDto.builder().memberSeq(memberSeq).
                workSpaceReq(workSpace.getWorkSpaceSeq()).friendList(teamWorkSpaceCreateReqDto.getFriendList()).build());
        return WorkSpaceResDto.fromEntity(workSpace);
    }

    // 프로젝트 워크스페이스 수정
    public WorkSpaceResDto editWorkSpace(TeamWorkSpaceEditReqDto teamWorkSpaceEditReqDto, Long memberSeq) throws AccessDeniedException {
        WorkSpace workSpace = workSpaceRepository.findById(teamWorkSpaceEditReqDto.getWorkSpaceSeq()).orElseThrow(() ->
                new EntityNotFoundException("해당 워크스페이스가 존재하지 않습니다."));
        // 권한 검증
        checkAuthority(workSpace, memberSeq);

        // 이름 수정
        workSpace.updateWorkSpaceName(teamWorkSpaceEditReqDto.getWorkSpaceName());
        // 썸네일 수정
        MultipartFile profileImage = teamWorkSpaceEditReqDto.getWorkSpaceThumbnailImage();
        if (profileImage != null && !profileImage.isEmpty()) {
            if (workSpace.getWorkSpaceThumbnailImageUrl() != null && !workSpace.getWorkSpaceThumbnailImageUrl().isEmpty()) {
                try {
                    s3Uploader.delete(workSpace.getWorkSpaceThumbnailImageUrl());
                } catch (Exception e) {
                    throw new IllegalArgumentException("S3 이미지 삭제에 실패했습니다.");
                }
            }
            String newThumbnailImageUrl = s3Uploader.upload(profileImage, WORKSPACE_THUMBNAIL_DIRECTORY);
            workSpace.updateImageUrl(newThumbnailImageUrl);
        }
        return WorkSpaceResDto.fromEntity(workSpace);
    }

    // 워크스페이스 삭제
    public void deleteWorkSpace(Long workSpaceSeq, Long memberSeq) throws Exception {
        WorkSpace workSpace = workSpaceRepository.findById(workSpaceSeq).orElseThrow(() ->
                new EntityNotFoundException("해당 워크스페이스가 존재하지 않습니다."));
        // 권한 검증
        checkAuthority(workSpace, memberSeq);

        // 워크스페이스 삭제
        workSpaceRepository.deleteById(workSpaceSeq);

        // 워크스페이스 정보 레디스에서 삭제
        workSpaceRedisService.removeWorkspaceFromMember(memberSeq, workSpaceSeq);
        workSpaceRedisService.removeWorkspace(workSpaceSeq);

        // 기본 채널 포함 채널 모두 삭제
        chatFeign.deleteAllChannel(workSpaceSeq);
        driveFeign.deleteTeamDrive(workSpaceSeq);
        taskFeign.deleteTaskChannel(workSpaceSeq);
        taskFeign.deleteAllVirtualMeetingChannel(workSpaceSeq);
    }

    // 각 기본 채널에 멤버 초대(워크스페이스에 처음 초대된 멤버일때)
    public void inviteWorkSpace(ChannelInviteReqDto channelInviteReqDto, Long memberSeq) throws AccessDeniedException {
        WorkSpace workSpace = workSpaceRepository.findById(channelInviteReqDto.getWorkSpaceSeq()).orElseThrow(() ->
                new EntityNotFoundException("해당 워크스페이스가 존재하지 않습니다."));

        // 권한 검증
        checkAuthority(workSpace, memberSeq);

        // 워크스페이스에 초대된 member정보 redis에 저장
        List<Long> invitefriendList = channelInviteReqDto.getFriendList();
        invitefriendList.stream().map(inviteMemberSeq -> memberRepository.findById(inviteMemberSeq)
                .orElseThrow(() -> new EntityNotFoundException("없는 회원입니다."))).forEach(inviteMember -> {
            workSpaceRedisService.addMemberInfo(inviteMember);
            workSpaceRedisService.addWorkSpace(workSpace, inviteMember.getMemberSeq());
            workSpaceRedisService.addMemberToWorkSpace(workSpace, inviteMember.getMemberSeq());
        });

        chatFeign.addMemberToChannel(channelInviteReqDto, memberSeq);
        taskFeign.addMemberToTaskChannel(channelInviteReqDto);
        taskFeign.addMemberToVirtualMeetingChannel(channelInviteReqDto, memberSeq);
    }

    // 프로젝트 SUPER 권한 위임
    public void delegateSuperAuthority(DelegateSuperAuthorityReqDto delegateSuperAuthorityReqDto, Long memberSeq) {
        WorkSpace workSpace = workSpaceRepository.findById(delegateSuperAuthorityReqDto.getWorkSpaceSeq())
                        .orElseThrow(() -> new EntityNotFoundException("유효하지 않은 워크스페이스입니다."));
        Member delegateMember = memberRepository.findById(delegateSuperAuthorityReqDto.getDelegateMemberSeq())
                        .orElseThrow(() -> new EntityNotFoundException("없는 회원입니다."));
        workSpace.updateSuperMember(delegateMember);
        chatFeign.delegateSuperAuthority(delegateSuperAuthorityReqDto, memberSeq);
        taskFeign.delegateTaskChannelSuperAuthority(delegateSuperAuthorityReqDto, memberSeq);
        taskFeign.delegateVirtualMeetChannelSuperAuthority(delegateSuperAuthorityReqDto, memberSeq);
    }

    // SUPER 권한 검증
    private void checkAuthority(WorkSpace workSpace, Long memberSeq) throws AccessDeniedException {
        if (!workSpace.getMember().getMemberSeq().equals(memberSeq)) {
            throw new AccessDeniedException("SUPER 권한이 아닙니다. 접근이 거부되었습니다.");
        }
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
//
//
}
