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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

        // 기본 채팅 채널 생성
        chatFeign.createChatBasicChannel(ChannelCreateReqDto.builder().channelName("일반").workSpaceSeq(workSpace.getWorkSpaceSeq())
                .memberSeq(workSpace.getMember().getMemberSeq()).memberList(teamWorkSpaceCreateReqDto.getMemberList()).build());

        // 기본 드라이브 생성
        driveFeign.createTeamDrive(DriveCreateReqDto.builder().workSpaceType(WorkSpaceType.PROJECT).workSpaceName(
                workSpace.getWorkSpaceName()).workSpaceReq(workSpace.getWorkSpaceSeq()).build());

        // 기본 화상회의 채널 생성
        taskFeign.createVirtualMeetBasicChannel(ChannelCreateReqDto.builder().channelName("일반").workSpaceSeq(
                        workSpace.getWorkSpaceSeq()).memberSeq(workSpace.getMember().getMemberSeq()).
                memberList(teamWorkSpaceCreateReqDto.getMemberList()).build());

        // 기본 task 생성
        taskFeign.createTask(TaskChannelMemberCreateReqDto.builder().memberSeq(memberSeq).
                workSpaceReq(workSpace.getWorkSpaceSeq()).memberList(teamWorkSpaceCreateReqDto.getMemberList()).build());

        // 워크스페이스 생성한 member정보 redis에 저장
        workSpaceRedisService.addMemberInfo(member);
        workSpaceRedisService.addWorkSpace(workSpace, member.getMemberSeq());
        workSpaceRedisService.addMemberToWorkSpace(workSpace, member.getMemberSeq());

        // 워크스페이스에 초대된 member정보 redis에 저장
        List<Long> inviteMemberList = Optional.ofNullable(teamWorkSpaceCreateReqDto.getMemberList())
                .orElse(Collections.emptyList());

        inviteMemberList.stream()
                .map(inviteMemberSeq -> memberRepository.findById(inviteMemberSeq)
                        .orElseThrow(() -> new EntityNotFoundException("없는 회원입니다.")))
                .forEach(inviteMember -> {
                    workSpaceRedisService.addMemberInfo(inviteMember);
                    workSpaceRedisService.addWorkSpace(workSpace, inviteMember.getMemberSeq());
                    workSpaceRedisService.addMemberToWorkSpace(workSpace, inviteMember.getMemberSeq());
                });


        return WorkSpaceResDto.fromEntity(workSpace);
    }

    // 개인 워크스페이스 대시보드 조회
    public PersonalDashBoardResDto findMyDashBoard(Long workSpaceSeq, Long memberSeq){
        // ToDo : 담당 개발자님이 개발(가져온 다음에 PersonalDashBoardResDto에 추가) or 개발되면 제가 API 가져와서 쓰겠습니다.
        // 1. 상단 통계 부분 개발
        // 진행중인 업무
        // 친구 요청
        // 읽지않은 메시지
        // ToDo : 이 부분은 알람 기능 개발하면서 제가 같이 개발하도록 하겠습니다.
        // 다가오는 일정(개인)

        // 2. 빠른 작업
        // ToDo : 빠른 작업은 이동 또는 기능 호출이므로 프론트에서 모두 처리하겠습니다.

        // 3. 최근 활동
        // ToDo : 이 부분은 알람 기능 개발하면서 제가 같이 개발하도록 하겠습니다.dkff

        // 4. 내가 참여중인 워크스페이스 목록
        List<WorkSpaceInfoResDto> myWorkSpaceList = findMyWorkSpaceList(memberSeq);

        return PersonalDashBoardResDto.of(myWorkSpaceList);
    }

    // 팀 워크스페이스 대시보드 조회
    public TeamDashBoardResDto findTeamDashBoard(Long workSpaceSeq){
        // ToDo : 담당 개발자님이 개발(가져온 다음에 TeamDashBoardResDto에 추가) or 개발되면 제가 API 가져와서 쓰겠습니다.
        // 1. 상단 통계 부분 개발
        // 전체 프로젝트 진행률
        // 진행중인 업무
        // 완료된 업무
        // 팀 멤버수 조회
        Long memberCount = findWorkSpaceMemberList(workSpaceSeq).stream().count();

        // 2. 프로젝트 진행 흐름
        // 전체 진행흐름(계획 / 실제 진행률)
        // 필터(월/주/일/사용자 지정)
        // 상세 진행 현황

        // 3. 마감일 / 마일스톤
        // 마감일 임박 업무(마감 5일전 업무)
        // 다가오는 마일스톤 목록

        // 4. 담당자별 업무 현황
        // 내 업무 현황
        // 다른 사용자의 업무 현황(필터)

        // 5. 최근 활동
        // ToDo : 이 부분은 알람 기능 개발하면서 같이 개발하도록 하겠습니다.
        return TeamDashBoardResDto.of();
    }

    // 내 워크스페이스 목록 조회
    public List<WorkSpaceInfoResDto> findMyWorkSpaceList(Long memberSeq) {
        List<?> myWorkSpaceList = workSpaceRedisService.findMyWorkSpaceList(memberSeq);

        // Redis 성공 케이스 (정상 DTO 조회)
        if (!myWorkSpaceList.isEmpty() && myWorkSpaceList.get(0) instanceof WorkSpaceInfoResDto) {
            return (List<WorkSpaceInfoResDto>) myWorkSpaceList;
        }

        // Redis 실패 케이스 (Fallback → Long 리스트)
        List<Long> seqList = myWorkSpaceList.stream()
                .filter(Objects::nonNull)
                .map(obj -> {
                    if (obj instanceof Long l) return l;
                    else if (obj instanceof Integer i) return i.longValue();
                    else return Long.parseLong(obj.toString());
                })
                .toList();

        // DB에서 개별 조회하여 DTO로 변환
        return seqList.stream()
                .map(workSpaceRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(ws -> ws.getWorkSpaceType() != WorkSpaceType.INDIVIDUAL)
                .map(WorkSpaceInfoResDto::fromEntity)
                .toList();
    }

    // 워크스페이스별 멤버 목록 조회
    public List<WorkSpaceMemberInfoResDto> findWorkSpaceMemberList(Long workSpaceSeq){
        List<?> workSpaceMemberList = workSpaceRedisService.findWorkSpaceMemberList(workSpaceSeq);

        // Redis 성공 케이스 (정상 DTO 조회)
        if (!workSpaceMemberList.isEmpty() && workSpaceMemberList.get(0) instanceof WorkSpaceMemberInfoResDto) {
            return (List<WorkSpaceMemberInfoResDto>) workSpaceMemberList;
        }

        // Redis 실패 케이스 (Fallback → Long 리스트)
        List<Long> seqList = workSpaceMemberList.stream()
                .filter(Objects::nonNull)
                .map(obj -> {
                    if (obj instanceof Long l) return l;
                    else if (obj instanceof Integer i) return i.longValue();
                    else return Long.parseLong(obj.toString());
                })
                .toList();

        // DB에서 개별 조회하여 DTO로 변환
        return seqList.stream()
                .map(memberRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(WorkSpaceMemberInfoResDto::fromEntity)
                .toList();
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

    // 워크스페이스 탈퇴
    public void leaveWorkSpace(Long workSpaceSeq, Long memberSeq) throws Exception {
        WorkSpace workSpace = workSpaceRepository.findById(workSpaceSeq).orElseThrow(() ->
                new EntityNotFoundException("해당 워크스페이스가 존재하지 않습니다."));

        // 강제 탈퇴
        if(workSpace.getMember().getMemberSeq().equals(memberSeq)){
            throw new IllegalStateException("SUPER 사용자는 탈퇴할 수 없습니다.");
        }

        // 사용자 검증
        List<WorkSpaceMemberInfoResDto> workSpaceMemberList = workSpaceRedisService.findWorkSpaceMemberList(workSpace.getWorkSpaceSeq());
        boolean isMemberIncluded = workSpaceMemberList.stream()
                .anyMatch(member -> Objects.equals(member.getMemberSeq(), memberSeq));

        if (!isMemberIncluded) {
            throw new AccessDeniedException("해당 워크스페이스의 멤버가 아닙니다.");
        }
        // 레디스 멤버 목록에서 워크스페이스 삭제
        workSpaceRedisService.removeWorkspaceFromMember(memberSeq, workSpace.getWorkSpaceSeq());
        // 레디스 워크스페이스 목록에서 워크스페이스 삭제
        workSpaceRedisService.removeMemberFromWorkSpace(memberSeq, workSpace.getWorkSpaceSeq());
        // 각 모듈 db에서 멤버 정보 삭제
        chatFeign.leaveWorkSpace(workSpace.getWorkSpaceSeq(), memberSeq);
        taskFeign.leaveWorkSpaceFromTask(workSpace.getWorkSpaceSeq(), memberSeq);
        taskFeign.leaveWorkSpaceFromVirtualMeeting(workSpace.getWorkSpaceSeq(), memberSeq);
    }

    // SUPER 사용자에 의한 워크스페이스 강제 탈퇴
    public void kickFromWorkSpace(KickMemberFromWorkSpaceReqDto kickMemberFromWorkSpaceReqDto, Long memberSeq) throws Exception {
        WorkSpace workSpace = workSpaceRepository.findById(kickMemberFromWorkSpaceReqDto.getWorkSpaceSeq()).orElseThrow(() ->
                new EntityNotFoundException("해당 워크스페이스가 존재하지 않습니다."));
        // 권한 검증
        checkAuthority(workSpace, memberSeq);

        // 탙뢰 대상 사용자 검증
        List<WorkSpaceMemberInfoResDto> workSpaceMemberList = workSpaceRedisService.findWorkSpaceMemberList(workSpace.getWorkSpaceSeq());
        boolean isMemberIncluded = workSpaceMemberList.stream()
                .anyMatch(member -> Objects.equals(member.getMemberSeq(), memberSeq));

        if (!isMemberIncluded) {
            throw new AccessDeniedException("해당 워크스페이스의 멤버가 아닙니다.");
        }
        // 레디스 멤버 목록에서 워크스페이스 삭제
        workSpaceRedisService.removeWorkspaceFromMember(kickMemberFromWorkSpaceReqDto.getMemberSeq(), workSpace.getWorkSpaceSeq());
        // 레디스 워크스페이스 목록에서 워크스페이스 삭제
        workSpaceRedisService.removeMemberFromWorkSpace(kickMemberFromWorkSpaceReqDto.getMemberSeq(), workSpace.getWorkSpaceSeq());
        // 각 모듈 db에서 멤버 정보 삭제
        chatFeign.kickFromWorkSpace(kickMemberFromWorkSpaceReqDto);
        taskFeign.kickFromWorkSpaceTask(kickMemberFromWorkSpaceReqDto);
        taskFeign.kickFromWorkSpaceVirtualMeeting(kickMemberFromWorkSpaceReqDto);
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
        List<Long> inviteMemberList = channelInviteReqDto.getMemberList();
        inviteMemberList.stream().map(inviteMemberSeq -> memberRepository.findById(inviteMemberSeq)
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
    public void delegateSuperAuthority(DelegateSuperAuthorityReqDto delegateSuperAuthorityReqDto, Long memberSeq) throws AccessDeniedException {
        WorkSpace workSpace = workSpaceRepository.findById(delegateSuperAuthorityReqDto.getWorkSpaceSeq())
                        .orElseThrow(() -> new EntityNotFoundException("유효하지 않은 워크스페이스입니다."));
        Member delegateMember = memberRepository.findById(delegateSuperAuthorityReqDto.getDelegateMemberSeq())
                        .orElseThrow(() -> new EntityNotFoundException("없는 회원입니다."));
        checkAuthority(workSpace, memberSeq);
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
//
//
//
}
