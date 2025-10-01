package com.team404.synco.workspace.service;

import com.team404.synco.common.constant.Authority;
import com.team404.synco.common.constant.WorkSpaceType;
import com.team404.synco.common.service.S3Uploader;
import com.team404.synco.member.entity.Member;
import com.team404.synco.member.repository.MemberRepository;
import com.team404.synco.workspace.dto.*;
import com.team404.synco.workspace.repository.WorkSpaceRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@Slf4j
public class WorkSpaceService {
    private final WorkSpaceRepository workSpaceRepository;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, Object> workSpaceRedisTemplate;
    private final S3Uploader s3Uploader;
    private final ChatFeign chatFeign;
    private final DriveFeign driveFeign;
    private final TaskFeign taskFeign;
    private String keyPrefix = "memberseq:";

    public WorkSpaceService(WorkSpaceRepository workSpaceRepository, MemberRepository memberRepository, @Qualifier("workSpaceInventory") RedisTemplate<String, Object> workSpaceRedisTemplate, S3Uploader s3Uploader, ChatFeign chatFeign, DriveFeign driveFeign, TaskFeign taskFeign) {
        this.workSpaceRepository = workSpaceRepository;
        this.memberRepository = memberRepository;
        this.workSpaceRedisTemplate = workSpaceRedisTemplate;
        this.s3Uploader = s3Uploader;
        this.chatFeign = chatFeign;
        this.driveFeign = driveFeign;
        this.taskFeign = taskFeign;
    }

    // 개인 워크스페이스 생성
    public Long createIndividualWorkSpace(WorkSpaceCreateReqDto workSpaceCreateReqDto, Long userId){
        // 로그 확인용
        log.info("요청값 : ", workSpaceCreateReqDto, userId);
        // 워크스페이스 프로필 이미지 업로드
        String workSpaceThumbnailImageUrl = addWorkSpaceThumbnailImageUrl(workSpaceCreateReqDto);

        // 멤버 불러오기
        Member member = memberRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("없는 회원입니다."));

        // 워크스페이스 생성
        long workSpaceSeq = workSpaceRepository.save(workSpaceCreateReqDto.toEntity(member, WorkSpaceType.INDIVIDUAL, workSpaceThumbnailImageUrl)).getWorkSpaceSeq();

        // workspace 정보 redis에 저장
        addWorkspace(userId, workSpaceSeq);

        // 개인 드라이브 생성
        driveFeign.createDriveChannel(
                DriveChannelCreateReqDto.builder()
                        .driveChannelName("내 드라이브")
                        .workspaceSeq(workSpaceSeq)
                        .build()
        );

        // 개인 일정관리 생성

        return workSpaceSeq;
    }

    // 팀 워크스페이스 생성
    public Long createTeamWorkSpace(WorkSpaceCreateReqDto workSpaceCreateReqDto, Long userId){
        // 워크스페이스 프로필 이미지 업로드
        String workSpaceThumbnailImageUrl = addWorkSpaceThumbnailImageUrl(workSpaceCreateReqDto);
        // 멤버 불러오기
        Member member = memberRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("없는 회원입니다."));
        // 워크스페이스 생성
        log.info("이미지 이름 : ", workSpaceThumbnailImageUrl);
        long workSpaceSeq = workSpaceRepository.save(workSpaceCreateReqDto.toEntity(member, WorkSpaceType.TEAM, workSpaceThumbnailImageUrl)).getWorkSpaceSeq();
        log.info("생성후 workspace seq: ", workSpaceSeq);
        // workspace 정보 redis에 저장
        addWorkspace(userId, workSpaceSeq);

        // 기본 채팅 채널 생성
        chatFeign.createChatChannel(
            ChatChannelCreateReqDto.builder()
                    .chatChannelName("일반")
                    .workSpaceSeq(workSpaceSeq)
                    .build()
        );

        // 기본 드라이브 채널 생성
        driveFeign.createDriveChannel(
            DriveChannelCreateReqDto.builder()
                    .driveChannelName("팀 드라이브")
                    .workspaceSeq(workSpaceSeq)
                    .build()
        );

        // 기본 화상회의 채널 생성
        taskFeign.createVirtualMeetChannel(
                VirtualMeetingChannelCreateReqDto.builder()
                        .virtualMeetingChannelName("회의")
                        .workSpaceSeq(workSpaceSeq)
                        .build()
        );

        // 기본 task 생성
        taskFeign.createTask(
            TaskCreateReqDto.builder()
                    .memberSeq(userId)
                    .authority(Authority.SUPER)
                    .build()
        );

        return workSpaceSeq;
    }

//    // 내 워크스페이스 목록 조회
//    public Page<WorkSpaceListResDto> myWorkSpaceList(){
//        return null;
//    }

//    // 워크스페이스 상세 조회
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
//    // 워크스페이스 초대전송
//    public WorkSpaceInviteResDto sendWorkSpaceInvite(){
//        return null;
//    }
//
//    // 워크스페이스 초대 목록
//    public Page<WorkSpaceListResDto> myWorkSpaceInviteList(){
//        return null;
//    }
//
//    // 워크스페이스 초대 승인
//    public Long approveWorkSpaceInvite(){
//        return null;
//    }

    // 워크스페이스 프로필 이미지 삽입
    private String addWorkSpaceThumbnailImageUrl(WorkSpaceCreateReqDto workSpaceCreateReqDto){
        String workSpaceThumbnailImageUrl = null;
        MultipartFile workSpaceThumbnailImage = workSpaceCreateReqDto.getWorkSpaceThumbnailImage();
        if (workSpaceThumbnailImage != null && !workSpaceThumbnailImage.isEmpty()) {
            workSpaceThumbnailImageUrl = s3Uploader.upload(workSpaceThumbnailImage, "workSpaceThumbnail");
        }
        return workSpaceThumbnailImageUrl;
    }

    // 워크스페이스 정보 redis에 추가
    private void addWorkspace(Long memberSeq, Long workspaceSeq) {
        log.info("workspace값: " , workspaceSeq);
        String key = keyPrefix + memberSeq + ":workspaces";
        workSpaceRedisTemplate.opsForSet().add(key, String.valueOf(workspaceSeq));
    }
}
