package com.team404.synco.workspace.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team404.synco.common.constant.Authority;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

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
    public WorkSpaceResDto createIndividualWorkSpace(WorkSpaceCreateReqDto workSpaceCreateReqDto, Long userId){
        // 워크스페이스 프로필 이미지 업로드
        String workSpaceThumbnailImageUrl = addWorkSpaceThumbnailImageUrl(workSpaceCreateReqDto);

        // 멤버 불러오기
        Member member = memberRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("없는 회원입니다."));

        // 워크스페이스 생성
        WorkSpace workSpace = workSpaceRepository.save(workSpaceCreateReqDto.toEntity(member, WorkSpaceType.INDIVIDUAL, workSpaceThumbnailImageUrl));

        // 개인 드라이브 생성
        driveFeign.createDriveChannel(
                DriveChannelCreateReqDto.builder()
                        .workspaceSeq(workSpace.getWorkSpaceSeq())
                        .build()
        );

        return WorkSpaceResDto.fromEntity(workSpace);
    }

    // 팀 워크스페이스 생성
    public WorkSpaceResDto createTeamWorkSpace(WorkSpaceCreateReqDto workSpaceCreateReqDto, Long userId){
        // 워크스페이스 프로필 이미지 업로드
        String workSpaceThumbnailImageUrl = addWorkSpaceThumbnailImageUrl(workSpaceCreateReqDto);
        // 워크스페이스 생성한 멤버 정보 불러오기
        Member member = memberRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("없는 회원입니다."));
        // 워크스페이스 생성
        WorkSpace workSpace = workSpaceRepository.save(workSpaceCreateReqDto.toEntity(member, WorkSpaceType.TEAM, workSpaceThumbnailImageUrl));

        // 워크스페이스 생성한 member정보 redis에 저장
        addMemberInfo(member);
        // 워크스페이스 정보 redis에 저장
        addWorkSpace(member, workSpace);

        // 기본 채팅 채널 생성
        chatFeign.createChatChannel(
            ChatChannelCreateReqDto.builder()
                    .chatChannelName("일반")
                    .workSpaceSeq(workSpace.getWorkSpaceSeq())
                    .build()
        );

        // 기본 드라이브 채널 생성
        driveFeign.createDriveChannel(
            DriveChannelCreateReqDto.builder()
                    .workspaceSeq(workSpace.getWorkSpaceSeq())
                    .build()
        );

        // 기본 화상회의 채널 생성
        taskFeign.createVirtualMeetChannel(
                VirtualMeetingChannelCreateReqDto.builder()
                        .virtualMeetingChannelName("회의")
                        .workSpaceSeq(workSpace.getWorkSpaceSeq())
                        .build()
        );

        // 기본 task 생성
        taskFeign.createTask(
            TaskCreateReqDto.builder()
                    .workSpaceSeq(workSpace.getWorkSpaceSeq())
                    .authority(Authority.SUPER)
                    .build()
        );


        return WorkSpaceResDto.fromEntity(workSpace);
    }

    // 워크스페이스 초대
    public WorkSpaceInviteResDto workSpaceInvite(WorkSpaceInviteReqDto workSpaceInviteReqDto){
        List<Long> memberList = workSpaceInviteReqDto.getMemberList();

        return null;
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
    private String addWorkSpaceThumbnailImageUrl(WorkSpaceCreateReqDto workSpaceCreateReqDto){
        String workSpaceThumbnailImageUrl = null;
        MultipartFile workSpaceThumbnailImage = workSpaceCreateReqDto.getWorkSpaceThumbnailImage();
        if (workSpaceThumbnailImage != null && !workSpaceThumbnailImage.isEmpty()) {
            workSpaceThumbnailImageUrl = s3Uploader.upload(workSpaceThumbnailImage, "workSpaceThumbnail");
        }
        return workSpaceThumbnailImageUrl;
    }

    // 멤버정보 redis에 추가
    private void addMemberInfo(Member member){
        String keyPrefix = "memberListeq:";
        String memberKey = keyPrefix + member.getMemberSeq();

        // 기존 member key 존재 여부 확인
        Boolean hasKey = workSpaceRedisTemplate.hasKey(memberKey);

        // key가 없을 경우 → 멤버 정보 처음 등록
        if (!hasKey) {
            workSpaceRedisTemplate.opsForHash().put(memberKey, "memberName", member.getName());
            workSpaceRedisTemplate.opsForHash().put(memberKey, "memberProfileUrl", member.getProfileImageUrl());
        }
    }

    // 워크스페이스 정보 redis에 추가
    private void addWorkSpace(Member member, WorkSpace workSpace) {
        String keyPrefix = "memberListeq:";
        String memberKey = keyPrefix + member.getMemberSeq();

        // workspaces 필드 가져오기
        Object existing = workSpaceRedisTemplate.opsForHash().get(memberKey, "workSpaces");
        List<Long> workSpaces = new ArrayList<>();

        if (existing != null) {
            try {
                workSpaces = new ObjectMapper().readValue(existing.toString(), new TypeReference<List<Long>>() {});
            } catch (Exception e) {
                log.error("워크스페이스 리스트 변환 실패: {}", e.getMessage());
            }
        }

        // 새로운 workspaceSeq 리스트에 추가
        workSpaces.add(workSpace.getWorkSpaceSeq());

        try {
            String json = new ObjectMapper().writeValueAsString(workSpaces);
            workSpaceRedisTemplate.opsForHash().put(memberKey, "workSpaces", json);
        } catch (Exception e) {
            log.error("워크스페이스 리스트 직렬화 실패 : {}", e.getMessage());
        }

        log.info("Redis 저장 완료: key={}, workSpaces={}", memberKey, workSpaces);
    }

    // 워크스페이스에 초대된 멤버 redis에 추가
    private void addMemberToWorkSpace(Member member, WorkSpace workSpace){
        String keyPrefix = "workSpaceSeq:";
        String workSpaceKey = keyPrefix + workSpace.getWorkSpaceSeq();

        // memberList 필드 가져오기
        Object existing = workSpaceRedisTemplate.opsForHash().get(workSpaceKey, "memberList");
        List<Long> memberList = new ArrayList<>();

        if (existing != null) {
            try {
                memberList = new ObjectMapper().readValue(existing.toString(), new TypeReference<List<Long>>() {});
            } catch (Exception e) {
                log.error("멤버 리스트 변환 실패: {}", e.getMessage());
            }
        }

        // 새로운 memberListeq 리스트에 추가
        memberList.add(member.getMemberSeq());

        try {
            String json = new ObjectMapper().writeValueAsString(memberList);
            workSpaceRedisTemplate.opsForHash().put(workSpaceKey, "memberList", json);
        } catch (Exception e) {
            log.error("멤버 리스트 직렬화 실패 : {}", e.getMessage());
        }

        log.info("Redis 저장 완료: key={}, memberList={}", workSpaceKey, memberList);
    }
}
