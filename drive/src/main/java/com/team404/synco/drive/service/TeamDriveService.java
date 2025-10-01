package com.team404.synco.drive.service;

import com.team404.synco.common.constant.WorkSpaceType;
import com.team404.synco.drive.dto.*;
import com.team404.synco.drive.entity.DriveChannel;
import com.team404.synco.drive.entity.Document;
import com.team404.synco.drive.repository.DocumentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TeamDriveService {

    private final CommonDriveService commonDriveService;
    private final DocumentRepository documentRepository;

    // 팀 드라이브 아이템 목록 조회
    public List<DriveItemDto> getTeamDriveItems(Long driveChannelSeq, Long parentFolderId, 
                                               String searchQuery, String sortBy, String sortOrder) {
        // 드라이브 채널 조회
        DriveChannel driveChannel = commonDriveService.getDriveChannel(driveChannelSeq);
        
        // 팀 드라이브인지 확인
        if (!driveChannel.getWorkspaceType().equals(WorkSpaceType.TEAM)) {
            throw new RuntimeException("팀 드라이브가 아닙니다.");
        }
        
        // TODO: 팀 멤버 권한 확인 로직 추가
        
        // 공통 서비스 사용
        return commonDriveService.getDriveItems(driveChannel, parentFolderId, searchQuery, sortBy, sortOrder);
    }

    // 팀 드라이브 폴더 생성
    public DriveItemDto createTeamFolder(Long userId, CreateFolderRequest request) {
        // 드라이브 채널 조회
        DriveChannel driveChannel = commonDriveService.getDriveChannel(request.getDriveChannelSeq());
        
        // 팀 드라이브인지 확인
        if (!driveChannel.getWorkspaceType().equals(WorkSpaceType.TEAM)) {
            throw new RuntimeException("팀 드라이브가 아닙니다.");
        }
        
        // TODO: 팀 멤버 권한 확인 로직 추가
        
        // 공통 서비스 사용
        return commonDriveService.createFolder(driveChannel, request.getFolderName(), request.getParentFolderId());
    }

    // 팀 드라이브 공유문서 생성
    public DriveItemDto createTeamSharedDoc(Long userId, CreateSharedDocRequest request) {
        // 드라이브 채널 조회
        DriveChannel driveChannel = commonDriveService.getDriveChannel(request.getDriveChannelSeq());
        
        // 팀 드라이브인지 확인
        if (!driveChannel.getWorkspaceType().equals(WorkSpaceType.TEAM)) {
            throw new RuntimeException("팀 드라이브가 아닙니다.");
        }
        
        // TODO: 팀 멤버 권한 확인 로직 추가
        
        // 팀 드라이브에서는 잠금 기능 사용 가능
        return commonDriveService.createSharedDoc(driveChannel, userId, request.getDocumentName(), 
            request.getParentFolderId(), request.getIsLocked(), request.getContent());
    }

    // 팀 드라이브 파일 업로드
    public List<DriveItemDto> uploadTeamFiles(Long userId, List<MultipartFile> files, Long driveChannelSeq, Long parentFolderId) {
        // 드라이브 채널 조회
        DriveChannel driveChannel = commonDriveService.getDriveChannel(driveChannelSeq);
        
        // 팀 드라이브인지 확인
        if (!driveChannel.getWorkspaceType().equals(WorkSpaceType.TEAM)) {
            throw new RuntimeException("팀 드라이브가 아닙니다.");
        }
        
        // TODO: 팀 멤버 권한 확인 로직 추가
        
        // 공통 서비스 사용
        return commonDriveService.uploadFiles(driveChannel, userId, files, parentFolderId);
    }

    // 팀 드라이브 아이템 이동
    public void moveTeamItem(Long userId, MoveItemRequest request) {
        // TODO: 팀 멤버 권한 확인 로직 추가
        
        // 공통 서비스 사용
        commonDriveService.moveItem(request.getItemType(), request.getItemId(), request.getNewParentId());
    }

    // 팀 드라이브 파일 다운로드
    public ResponseEntity<byte[]> downloadTeamFile(Long userId, Long documentSeq) {
        // TODO: 팀 멤버 권한 확인 로직 추가
        
        Document document = documentRepository.findById(documentSeq)
            .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다."));
        
        try {
            Path filePath = Paths.get(document.getDocumentUrl());
            byte[] fileContent = Files.readAllBytes(filePath);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", document.getDocumentName());
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(fileContent);
                
        } catch (IOException e) {
            log.error("파일 다운로드 실패: {}", document.getDocumentName(), e);
            throw new RuntimeException("파일 다운로드에 실패했습니다.", e);
        }
    }

    // 팀 드라이브 아이템 삭제
    public void deleteTeamItem(Long userId, String itemType, Long itemId) {
        // TODO: 팀 멤버 권한 확인 로직 추가
        
        // 공통 서비스 사용
        commonDriveService.deleteItem(itemType, itemId);
    }

    // 팀 드라이브 채널 생성
    public DriveItemDto createTeamDriveChannel(Long userId, CreateDriveChannelRequest request) {
        DriveChannel channel = DriveChannel.builder()
            .driveChannelName(request.getDriveChannelName())
            .workspaceSeq(request.getWorkspaceSeq())
            .workspaceType(WorkSpaceType.TEAM)
            .build();
        
        // TODO: 팀 멤버 권한 확인 로직 추가
        
        // 공통 서비스 사용
        return commonDriveService.convertDriveChannelToDto(channel);
    }
}
