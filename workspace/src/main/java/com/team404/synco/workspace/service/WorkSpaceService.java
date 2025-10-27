package com.team404.synco.workspace.service;

import com.team404.synco.common.constant.Authority;
import com.team404.synco.common.constant.WorkSpaceType;
import com.team404.synco.common.service.S3Uploader;
import com.team404.synco.member.entity.Member;
import com.team404.synco.member.repository.MemberRepository;
import com.team404.synco.workspace.dto.*;
import com.team404.synco.workspace.entity.WorkSpace;
import com.team404.synco.workspace.repository.WorkSpaceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

        // 프로젝트 생성
        WorkSpace workSpace = workSpaceRepository.save(WorkSpace.builder().member(member).workSpaceName(member.getName()).
                workSpaceThumbnailImageUrl(member.getProfileImageUrl()).workSpaceType(WorkSpaceType.INDIVIDUAL).build());

        // 프로젝트 생성한 member정보 redis에 저장
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

    // 프로젝트 생성
    public WorkSpaceResDto createTeamWorkSpace(TeamWorkSpaceCreateReqDto teamWorkSpaceCreateReqDto, Long memberSeq) {
        // 프로젝트 생성한 멤버 정보 불러오기
        Member member = memberRepository.findById(memberSeq).orElseThrow(() -> new EntityNotFoundException("없는 회원입니다."));
        // 프로젝트 썸네일 이미지 업로드
        String workSpaceThumbnailImageUrl = null;
        if (teamWorkSpaceCreateReqDto.getWorkSpaceThumbnailImage() != null &&
                !teamWorkSpaceCreateReqDto.getWorkSpaceThumbnailImage().isEmpty()) {
            workSpaceThumbnailImageUrl = s3Uploader.upload(teamWorkSpaceCreateReqDto.getWorkSpaceThumbnailImage()
                    , WORKSPACE_THUMBNAIL_DIRECTORY);
        }
        // 프로젝트 생성
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

        // 프로젝트 생성한 member정보 redis에 저장
        workSpaceRedisService.addMemberInfo(member);
        workSpaceRedisService.addWorkSpace(workSpace, member.getMemberSeq());
        workSpaceRedisService.addMemberToWorkSpace(workSpace, member.getMemberSeq());

        // 프로젝트에 초대된 member정보 redis에 저장
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
    @Transactional(readOnly = true)
    public WorkSpaceInfoResDto findPersonalWorkSpace(Long memberSeq) {
        Member member = memberRepository.findById(memberSeq).orElseThrow(() -> new EntityNotFoundException("회원정보가 존재하지 않습니다."));
        WorkSpace workSpace = workSpaceRepository.findByMemberAndWorkSpaceType(member, WorkSpaceType.INDIVIDUAL)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 개인 워크스페이스입니다."));
        return WorkSpaceInfoResDto.fromEntity(workSpace);
    }

    // 내 프로젝트 목록 조회
    @Transactional(readOnly = true)
    public List<WorkSpaceInfoResDto> findMyWorkSpaceList(Long memberSeq) {
        List<?> myWorkSpaceList = workSpaceRedisService.findMyWorkSpaceList(memberSeq);

        // Redis 성공 케이스 (정상 DTO 조회)
        if (!myWorkSpaceList.isEmpty() && myWorkSpaceList.get(0) instanceof WorkSpaceInfoResDto) {
            return (List<WorkSpaceInfoResDto>) myWorkSpaceList;
        }

        // DB에서 개별 조회하여 DTO로 변환
        if (!myWorkSpaceList.isEmpty() && myWorkSpaceList.get(0) instanceof Long) {
            log.info("Redis 조회 실패 → Feign fallback 결과(Long 리스트)");
            List<Long> seqList = (List<Long>) myWorkSpaceList;

            return seqList.stream()
                    .skip(1)
                    .map(workSpaceRepository::findById)
                    .flatMap(Optional::stream)
                    .map(WorkSpaceInfoResDto::fromEntity)
                    .toList();
        }

        // 기타 예외 상황 (비어있거나 예측 불가 타입)
        log.warn("Redis 조회 결과가 비어있거나 예측 불가한 타입입니다. (type={})",
                myWorkSpaceList.isEmpty() ? "EMPTY" : myWorkSpaceList.get(0).getClass().getName());
        return Collections.emptyList();
    }

    // 프로젝트 멤버목록
    @Transactional(readOnly = true)
    public List<WorkSpaceMemberInfoResDto> findWorkSpaceMemberList(Long workSpaceSeq) {
        WorkSpace workSpace = workSpaceRepository.findById(workSpaceSeq).orElseThrow(() ->
                new EntityNotFoundException("해당 프로젝트가 존재하지 않습니다."));
        Member superMember = workSpace.getMember();
        List<?> workSpaceMemberList = workSpaceRedisService.findWorkSpaceMemberList(workSpaceSeq, superMember.getMemberSeq());

        // Redis 성공 (DTO 타입)
        if (!workSpaceMemberList.isEmpty() && workSpaceMemberList.get(0) instanceof WorkSpaceMemberInfoResDto dto) {
            return (List<WorkSpaceMemberInfoResDto>) workSpaceMemberList;
        }

        // Redis 실패 (Fallback으로 Long 타입 리스트)
        if (!workSpaceMemberList.isEmpty() && workSpaceMemberList.get(0) instanceof Long) {
            log.info("Redis 조회 실패 → Feign fallback 결과(Long 리스트)");
            List<Long> seqList = (List<Long>) workSpaceMemberList;

            return seqList.stream()
                    .map(memberRepository::findById)
                    .flatMap(Optional::stream)
                    .map(member -> {
                        Authority authority = member.getMemberSeq().equals(superMember.getMemberSeq())
                                ? Authority.SUPER
                                : Authority.PARTICIPANT;
                        return WorkSpaceMemberInfoResDto.of(member, authority);
                    })
                    .toList();
        }

        // 기타 예외 상황 (비어있거나 예측 불가 타입)
        log.warn("Redis 조회 결과가 비어있거나 예측 불가한 타입입니다. (type={})",
                workSpaceMemberList.isEmpty() ? "EMPTY" : workSpaceMemberList.get(0).getClass().getName());
        return Collections.emptyList();
    }


    // 프로젝트 수정
    public WorkSpaceResDto editWorkSpace(TeamWorkSpaceEditReqDto teamWorkSpaceEditReqDto, Long memberSeq)
            throws AccessDeniedException {
        WorkSpace workSpace = workSpaceRepository.findById(teamWorkSpaceEditReqDto.getWorkSpaceSeq())
                .orElseThrow(() -> new EntityNotFoundException("해당 프로젝트가 존재하지 않습니다."));

        // 권한 검증
        checkAuthority(workSpace, memberSeq);

        MultipartFile profileImage = teamWorkSpaceEditReqDto.getWorkSpaceThumbnailImage();
        log.info("수정할 이미지 : {}", profileImage);

        // 이름 수정
        String newName = teamWorkSpaceEditReqDto.getWorkSpaceName();
        workSpace.updateWorkSpaceName(newName);

        String newThumbnailImageUrl = workSpace.getWorkSpaceThumbnailImageUrl();

        // 썸네일이 null 또는 비어 있으면 S3 / DB 수정 전부 skip
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                // 기존 이미지가 존재할 경우 S3에서 삭제
                if (newThumbnailImageUrl != null && !newThumbnailImageUrl.isEmpty()) {
                    s3Uploader.delete(newThumbnailImageUrl);
                }

                // 새 이미지 업로드
                newThumbnailImageUrl = s3Uploader.upload(profileImage, WORKSPACE_THUMBNAIL_DIRECTORY);
                workSpace.updateImageUrl(newThumbnailImageUrl);

            } catch (Exception e) {
                throw new IllegalArgumentException("워크스페이스 썸네일 수정 중 오류가 발생했습니다: " + e.getMessage());
            }
        } else {
            log.info("썸네일이 null 또는 비어 있으므로 S3 및 DB 수정 건너뜀");
        }

        // redis 반영
        workSpaceRedisService.editWorkSpaceInfo(
                workSpace.getWorkSpaceSeq(),
                newName,
                newThumbnailImageUrl
        );
        return WorkSpaceResDto.fromEntity(workSpace);
    }


    // 프로젝트 탈퇴
    public void leaveWorkSpace(Long workSpaceSeq, Long memberSeq) throws Exception {
        WorkSpace workSpace = workSpaceRepository.findById(workSpaceSeq).orElseThrow(() ->
                new EntityNotFoundException("해당 프로젝트가 존재하지 않습니다."));

        // 강제 탈퇴
        if (workSpace.getMember().getMemberSeq().equals(memberSeq)) {
            throw new IllegalStateException("SUPER 사용자는 탈퇴할 수 없습니다.");
        }

        // 사용자 검증
        List<WorkSpaceMemberInfoResDto> workSpaceMemberList = workSpaceRedisService.findWorkSpaceMemberList(workSpace.getWorkSpaceSeq()
                , workSpace.getMember().getMemberSeq());
        boolean isMemberIncluded = workSpaceMemberList.stream()
                .anyMatch(member -> Objects.equals(member.getMemberSeq(), memberSeq));

        if (!isMemberIncluded) {
            throw new AccessDeniedException("해당 프로젝트의 멤버가 아닙니다.");
        }
        // 레디스 멤버 목록에서 프로젝트 삭제
        workSpaceRedisService.removeWorkspaceFromMember(memberSeq, workSpace.getWorkSpaceSeq());
        // 레디스 프로젝트 목록에서 프로젝트 삭제
        workSpaceRedisService.removeMemberFromWorkSpace(memberSeq, workSpace.getWorkSpaceSeq());
        // 각 모듈 db에서 멤버 정보 삭제
        chatFeign.leaveWorkSpace(workSpace.getWorkSpaceSeq(), memberSeq);
        taskFeign.leaveWorkSpaceFromTask(workSpace.getWorkSpaceSeq(), memberSeq);
        taskFeign.leaveWorkSpaceFromVirtualMeeting(workSpace.getWorkSpaceSeq(), memberSeq);
    }

    // SUPER 사용자에 의한 프로젝트 강제 탈퇴
    public void kickFromWorkSpace(KickMemberFromWorkSpaceReqDto kickMemberFromWorkSpaceReqDto, Long memberSeq) throws Exception {
        WorkSpace workSpace = workSpaceRepository.findById(kickMemberFromWorkSpaceReqDto.getWorkSpaceSeq()).orElseThrow(() ->
                new EntityNotFoundException("해당 프로젝트가 존재하지 않습니다."));
        // 권한 검증
        checkAuthority(workSpace, memberSeq);

        // 탙뢰 대상 사용자 검증
        List<WorkSpaceMemberInfoResDto> workSpaceMemberList = workSpaceRedisService.findWorkSpaceMemberList(workSpace.getWorkSpaceSeq(),
                workSpace.getMember().getMemberSeq());
        boolean isMemberIncluded = workSpaceMemberList.stream()
                .anyMatch(member -> Objects.equals(member.getMemberSeq(), memberSeq));

        if (!isMemberIncluded) {
            throw new AccessDeniedException("해당 프로젝트의 멤버가 아닙니다.");
        }
        // 레디스 멤버 목록에서 프로젝트 삭제
        workSpaceRedisService.removeWorkspaceFromMember(kickMemberFromWorkSpaceReqDto.getMemberSeq(), workSpace.getWorkSpaceSeq());
        // 레디스 프로젝트 목록에서 프로젝트 삭제
        workSpaceRedisService.removeMemberFromWorkSpace(kickMemberFromWorkSpaceReqDto.getMemberSeq(), workSpace.getWorkSpaceSeq());
        // 각 모듈 db에서 멤버 정보 삭제
        chatFeign.kickFromWorkSpace(kickMemberFromWorkSpaceReqDto);
        taskFeign.kickFromWorkSpaceTask(kickMemberFromWorkSpaceReqDto);
        taskFeign.kickFromWorkSpaceVirtualMeeting(kickMemberFromWorkSpaceReqDto);
    }


    // 프로젝트 삭제
    public void deleteWorkSpace(Long workSpaceSeq, Long memberSeq) throws Exception {
        WorkSpace workSpace = workSpaceRepository.findById(workSpaceSeq).orElseThrow(() ->
                new EntityNotFoundException("해당 프로젝트가 존재하지 않습니다."));
        // 권한 검증
        checkAuthority(workSpace, memberSeq);

        // 프로젝트 삭제
        workSpaceRepository.deleteById(workSpaceSeq);

        // 프로젝트 정보 레디스에서 삭제
        workSpaceRedisService.removeWorkspaceFromMember(memberSeq, workSpaceSeq);
        workSpaceRedisService.removeWorkspace(workSpaceSeq);

        // 기본 채널 포함 채널 모두 삭제
        chatFeign.deleteAllChannel(workSpaceSeq);
        driveFeign.deleteTeamDrive(workSpaceSeq);
        taskFeign.deleteTaskChannel(workSpaceSeq);
        taskFeign.deleteAllVirtualMeetingChannel(workSpaceSeq);
    }

    // 각 기본 채널에 멤버 초대(프로젝트에 처음 초대된 멤버일때)
    public void inviteWorkSpace(ChannelInviteReqDto channelInviteReqDto, Long memberSeq) throws AccessDeniedException {
        WorkSpace workSpace = workSpaceRepository.findById(channelInviteReqDto.getWorkSpaceSeq()).orElseThrow(() ->
                new EntityNotFoundException("해당 프로젝트가 존재하지 않습니다."));

        // 권한 검증
        checkAuthority(workSpace, memberSeq);

        // 프로젝트에 초대된 member정보 redis에 저장
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
    public void delegateSuperAuthority(DelegateSuperAuthorityReqDto delegateSuperAuthorityReqDto, Long memberSeq)
            throws AccessDeniedException {
        WorkSpace workSpace = workSpaceRepository.findById(delegateSuperAuthorityReqDto.getWorkSpaceSeq())
                .orElseThrow(() -> new EntityNotFoundException("유효하지 않은 프로젝트입니다."));

        Member superMember = memberRepository.findById(memberSeq)
                .orElseThrow(() -> new EntityNotFoundException("없는 회원입니다."));
        Member delegateMember = memberRepository.findById(delegateSuperAuthorityReqDto.getDelegateMemberSeq())
                .orElseThrow(() -> new EntityNotFoundException("없는 회원입니다."));

        checkAuthority(workSpace, memberSeq);
        workSpace.updateSuperMember(delegateMember);

        workSpaceRedisService.addMemberInfo(delegateMember);
        workSpaceRedisService.addMemberInfo(superMember);

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
