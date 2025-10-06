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
import org.springframework.data.redis.serializer.SerializationException;
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
    private final RedisTemplate<String, Object> memberRedisTemplate;
    private final RedisTemplate<String, Object> workSpaceRedisTemplate;
    private final S3Uploader s3Uploader;
    private final ChatFeign chatFeign;
    private final DriveFeign driveFeign;
    private final TaskFeign taskFeign;

    public WorkSpaceService(WorkSpaceRepository workSpaceRepository, MemberRepository memberRepository, @Qualifier("memberInventory") RedisTemplate<String, Object> memberRedisTemplate, @Qualifier("workSpaceInventory") RedisTemplate<String, Object> workSpaceRedisTemplate, S3Uploader s3Uploader, ChatFeign chatFeign, DriveFeign driveFeign, TaskFeign taskFeign) {
        this.workSpaceRepository = workSpaceRepository;
        this.memberRepository = memberRepository;
        this.memberRedisTemplate = memberRedisTemplate;
        this.workSpaceRedisTemplate = workSpaceRedisTemplate;
        this.s3Uploader = s3Uploader;
        this.chatFeign = chatFeign;
        this.driveFeign = driveFeign;
        this.taskFeign = taskFeign;
    }

    // 개인 워크스페이스 생성
    public WorkSpaceResDto createIndividualWorkSpace(Long memberSeq) {
        // 멤버 불러오기
        Member member = memberRepository.findById(memberSeq).orElseThrow(() -> new EntityNotFoundException("없는 회원입니다."));

        // 워크스페이스 객체 조립
        IndividualWorkSpaceCreateReqDto individualWorkSpaceCreateReqDto = IndividualWorkSpaceCreateReqDto.builder()
                .workSpaceName(member.getName()+"님의 워크스페이스")
                .workSpaceThumbnailImage(member.getProfileImageUrl())
                .build();

        // 워크스페이스 생성
        WorkSpace workSpace = workSpaceRepository.save(individualWorkSpaceCreateReqDto.toEntity(member, WorkSpaceType.INDIVIDUAL));

        // 개인 드라이브 생성
        driveFeign.createDrive(
                DriveCreateReqDto.builder()
                        .workSpaceType(WorkSpaceType.INDIVIDUAL)
                        .workSpaceName(workSpace.getWorkSpaceName())
                        .workSpaceReq(workSpace.getWorkSpaceSeq())
                        .build()
        );

        // 개인 일정 생성
        taskFeign.createTask(
                TaskChannelMemberCreateReqDto.builder()
                        .memberSeq(memberSeq)
                        .authority(Authority.SUPER)
                        .workSpaceReq(workSpace.getWorkSpaceSeq())
                        .build()
        );

        return WorkSpaceResDto.fromEntity(workSpace);
    }

    // 팀 워크스페이스 생성
    public WorkSpaceResDto createTeamWorkSpace(TeamWorkSpaceCreateReqDto teamWorkSpaceCreateReqDto, Long memberSeq) {
        // 워크스페이스 프로필 이미지 업로드
        String workSpaceThumbnailImageUrl = addWorkSpaceThumbnailImageUrl(teamWorkSpaceCreateReqDto.getWorkSpaceThumbnailImage());
        // 워크스페이스 생성한 멤버 정보 불러오기
        Member member = memberRepository.findById(memberSeq).orElseThrow(() -> new EntityNotFoundException("없는 회원입니다."));
        // 워크스페이스 생성
        WorkSpace workSpace = workSpaceRepository.save(teamWorkSpaceCreateReqDto.toEntity(member, WorkSpaceType.TEAM, workSpaceThumbnailImageUrl));
        // 워크스페이스 생성한 member정보 redis에 저장
        addMemberInfo(member);
        addWorkSpace(member, workSpace);
        addMemberToWorkSpace(memberSeq, workSpace);

        // 워크스페이스에 초대된 member정보 redis에 저장
        List<Long> inviteMemberList = teamWorkSpaceCreateReqDto.getMemberList();
        for(Long inviteMemberSeq : inviteMemberList){
            Member inviteMember = memberRepository.findById(inviteMemberSeq).orElseThrow(() -> new EntityNotFoundException("없는 회원입니다."));
            addMemberInfo(inviteMember);
            addWorkSpace(inviteMember, workSpace);
            addMemberToWorkSpace(inviteMemberSeq, workSpace);
        }


        // 기본 채팅 채널 생성
        chatFeign.createChatChannel(
                ChannelCreateReqDto.builder()
                        .ChannelName("일반")
                        .workSpaceSeq(workSpace.getWorkSpaceSeq())
                        .memberSeq(memberSeq)
                        .memberList(teamWorkSpaceCreateReqDto.getMemberList())
                        .build()
        );

        // 기본 드라이브 생성
        driveFeign.createDrive(
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
                        .memberSeq(memberSeq)
                        .memberList(teamWorkSpaceCreateReqDto.getMemberList())
                        .build()
        );

        // 기본 task 생성
        taskFeign.createTask(
                TaskChannelMemberCreateReqDto.builder()
                        .memberSeq(memberSeq)
                        .authority(Authority.SUPER)
                        .workSpaceReq(workSpace.getWorkSpaceSeq())
                        .build()
        );
        return WorkSpaceResDto.fromEntity(workSpace);
    }

    // 각 채널에 멤버 초대
    public void channelInvite(ChannelInviteReqDto channelInviteReqDto) {
        chatFeign.addMemberToChannel(channelInviteReqDto);
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

    // 멤버 정보 redis에 추가
    private void addMemberInfo(Member member) {
        String keyPrefix = "memberListSeq:";
        String memberKey = keyPrefix + member.getMemberSeq();

        // 기존 member key 존재 여부 확인
        Boolean hasKey = memberRedisTemplate.hasKey(memberKey);

        // key가 없을 경우 → 멤버 정보 처음 등록
        if (!hasKey) {
            memberRedisTemplate.opsForHash().put(memberKey, "memberName", member.getName());
            memberRedisTemplate.opsForHash().put(memberKey, "memberProfileUrl", member.getProfileImageUrl());
        }
    }

    // 워크스페이스 정보 redis에 추가
    private void addWorkSpace(Member member, WorkSpace workSpace) {
        String keyPrefix = "memberListSeq:";
        String memberKey = keyPrefix + member.getMemberSeq();

        // workspaces 필드 가져오기
        Object existing = memberRedisTemplate.opsForHash().get(memberKey, "workSpaceList");
        List<Long> workSpaces = new ArrayList<>();

        if (existing != null) {
            try {
                workSpaces = new ObjectMapper().readValue(existing.toString(), new TypeReference<List<Long>>() {
                });
            } catch (Exception e) {
                throw new SerializationException("직렬화에 실패하였습니다.");
            }
        }

        // 새로운 workspaceSeq 리스트에 추가
        workSpaces.add(workSpace.getWorkSpaceSeq());

        try {
            String json = new ObjectMapper().writeValueAsString(workSpaces);
            memberRedisTemplate.opsForHash().put(memberKey, "workSpaceList", json);
        } catch (Exception e) {
            throw new SerializationException("직렬화에 실패하였습니다.");
        }
    }

    // 워크스페이스에 초대된 멤버 redis에 추가
    private void addMemberToWorkSpace(Long memberSeq, WorkSpace workSpace) {
        String keyPrefix = "workSpaceSeq:";
        String workSpaceKey = keyPrefix + workSpace.getWorkSpaceSeq();

        // memberList 필드 가져오기
        Object existing = workSpaceRedisTemplate.opsForHash().get(workSpaceKey, "memberList");
        List<Long> memberList = new ArrayList<>();

        if (existing != null) {
            try {
                memberList = new ObjectMapper().readValue(existing.toString(), new TypeReference<List<Long>>() {
                });
            } catch (Exception e) {
                throw new SerializationException("직렬화에 실패하였습니다.");
            }
        }

        // 새로운 memberListeq 리스트에 추가
        memberList.add(memberSeq);

        try {
            String json = new ObjectMapper().writeValueAsString(memberList);
            workSpaceRedisTemplate.opsForHash().put(workSpaceKey, "memberList", json);
        } catch (Exception e) {
            throw new SerializationException("직렬화에 실패하였습니다.");
        }
    }
}
