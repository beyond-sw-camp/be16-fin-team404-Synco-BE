package com.team404.synco.drive.service;

import com.team404.synco.drive.dto.DriveChannelCreateReqDto;
import com.team404.synco.drive.repository.DriveChannelRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class DriveService {
    private final DriveChannelRepository driveChannelRepository;

    public DriveService(DriveChannelRepository driveChannelRepository) {
        this.driveChannelRepository = driveChannelRepository;
    }

    // 채널 생성
    // ToDO : 우선은 기본 채널만 생성되도록 작업했습니다. 추후 채널 추가 가능하도록 코드 수정 예정입니다.
    public Long createChannel(DriveChannelCreateReqDto driveChannelCreateReqDto){
        return driveChannelRepository.save(driveChannelCreateReqDto.toEntity(driveChannelCreateReqDto.getWorkspaceSeq())).getDriveChannelSeq();
    }
}
