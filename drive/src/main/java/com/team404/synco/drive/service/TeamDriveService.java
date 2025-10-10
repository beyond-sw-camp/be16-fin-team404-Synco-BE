package com.team404.synco.drive.service;

import com.team404.synco.drive.dto.DriveCreateReqDto;
import com.team404.synco.drive.repository.DriveChannelRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class TeamDriveService {
    private final DriveChannelRepository driveChannelRepository;

    public TeamDriveService(DriveChannelRepository driveChannelRepository) {
        this.driveChannelRepository = driveChannelRepository;
    }

    // 드라이브 생성
    public Long createChannel(DriveCreateReqDto driveCreateReqDto){
        return driveChannelRepository.save(driveCreateReqDto.toEntity()).getDriveChannelSeq();
    }

    // 드라이브 삭제(WorkSpace 삭제시)
    public void deleteDrive(Long workSpaceSeq){
        driveChannelRepository.deleteByWorkspaceSeq(workSpaceSeq);
    }
}
