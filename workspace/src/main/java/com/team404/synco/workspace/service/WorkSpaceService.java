package com.team404.synco.workspace.service;

import com.team404.synco.common.constant.WorkSpaceType;
import com.team404.synco.workspace.dto.*;
import com.team404.synco.workspace.entity.WorkSpace;
import com.team404.synco.workspace.repository.WorkSpaceRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class WorkSpaceService {
    private final WorkSpaceRepository workSpaceRepository;
    private final RedisTemplate<String, Object> workSpaceRedisTemplate;
    private final ChatFeign chatFeign;
    private final DriveFeign driveFeign;
    private final TaskFeign taskFeign;

    public WorkSpaceService(WorkSpaceRepository workSpaceRepository, @Qualifier("workSpaceInventory") RedisTemplate<String, Object> workSpaceRedisTemplate, ChatFeign chatFeign, DriveFeign driveFeign, TaskFeign taskFeign) {
        this.workSpaceRepository = workSpaceRepository;
        this.workSpaceRedisTemplate = workSpaceRedisTemplate;
        this.chatFeign = chatFeign;
        this.driveFeign = driveFeign;
        this.taskFeign = taskFeign;
    }

    // 개인 워크스페이스 생성
    public Long createIndividualWorkSpace(WorkSpaceCreateReqDto workSpaceCreateReqDto){
        WorkSpace workSpace;
        // 워크스페이스 생성
        workSpace = workSpaceRepository.save(workSpaceCreateReqDto.toEntity(WorkSpaceType.INDIVIDUAL));
        return workSpace.getWorkSpaceSeq();
    }

    // 팀 워크스페이스 생성
    public Long createTeamWorkSpace(WorkSpaceCreateReqDto workSpaceCreateReqDto){
        WorkSpace workSpace;
        // 워크스페이스 생성
        workSpace = workSpaceRepository.save(workSpaceCreateReqDto.toEntity(WorkSpaceType.TEAM));

        // workspace 정보 redis에 저장
        workSpaceRedisTemplate.opsForValue().set(1L, );

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
                    .driveChannelName("팀 드라이브")
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

        // 기본 task(상태 보드, 사용자별 커스텀 보드 생성

        return workSpace.getWorkSpaceSeq();
    }

    // 내 워크스페이스 목록 조회
    public Page<WorkSpaceListResDto> myWorkSpaceList(){
        return null;
    }

    // 워크스페이스 상세 조회
    public WorkSpaceDetailResDto workSpaceDetail(){
        return null;
    }

    // 워크스페이스 수정
    public WorkSpaceEditResDto editWorkSpace(){
        return null;
    }

    // 워크스페이스 삭제
    public Long deleteWorkSpace(){
        return null;
    }

    // 워크스페이스 초대전송
    public WorkSpaceInviteResDto sendWorkSpaceInvite(){
        return null;
    }

    // 워크스페이스 초대 목록
    public Page<WorkSpaceListResDto> myWorkSpaceInviteList(){
        return null;
    }

    // 워크스페이스 초대 승인
    public Long approveWorkSpaceInvite(){
        return null;
    }
}
