package com.team404.synco.drive.service;

import com.team404.synco.drive.dto.*;
import com.team404.synco.drive.entity.DriveChannel;
import com.team404.synco.drive.entity.Document;
import com.team404.synco.drive.repository.DocumentRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<DriveItemDto> getTeamDriveItems(Long driveChannelSeq, Long parentFolderId, Pageable pageable, String nameFilter, String modifiedDateFilter, String byteSizeFilter) {
        DriveChannel driveChannel = commonDriveService.getDriveChannel(driveChannelSeq);
        return commonDriveService.getDriveItems(driveChannel, parentFolderId, pageable, nameFilter, modifiedDateFilter, byteSizeFilter);
    }

    // 팀 드라이브 폴더 생성
    public DriveItemDto createTeamFolder(Long userId, CreateFolderRequest request) {
        DriveChannel driveChannel = commonDriveService.getDriveChannel(request.getDriveChannelSeq());
        return commonDriveService.createFolder(driveChannel, request.getFolderName(), request.getParentFolderSeq());
    }

    // 팀 드라이브 공유문서 생성
    public DriveItemDto createTeamSharedDoc(Long userId, CreateSharedDocRequest request) {
        commonDriveService.getDriveChannel(request.getDriveChannelSeq());
        return commonDriveService.createSharedDoc(userId, request.getDocumentName(), request.getParentFolderSeq(), request.getIsLocked());
    }

    // 팀 드라이브 파일 업로드
    public List<DriveItemDto> uploadTeamFiles(Long userId, List<MultipartFile> files, Long driveChannelSeq, Long parentFolderId) {
        DriveChannel driveChannel = commonDriveService.getDriveChannel(driveChannelSeq);
        return commonDriveService.uploadFiles(driveChannel, userId, files, parentFolderId);
    }

    // 팀 드라이브 아이템 이동
    public void moveTeamItem(MoveItemRequest request) {
        commonDriveService.moveItem(request.getItemType(), request.getItemId(), request.getNewParentSeq());
    }

    // 팀 드라이브 아이템 순서 변경
    public void reorderTeamItem(ReorderItemRequest request) {
        commonDriveService.reorderItem(request.getItemType(), request.getItemId(), request.getNewOrder());
    }

    // 팀 드라이브 파일 다운로드
    public ResponseEntity<byte[]> downloadTeamFile(Long documentSeq) {
        Document document = documentRepository.findById(documentSeq).orElseThrow(() -> new EntityNotFoundException("파일을 찾을 수 없습니다."));

        try {
            Path filePath = Paths.get(document.getDocumentUrl());
            byte[] fileContent = Files.readAllBytes(filePath);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", document.getDocumentName());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileContent);

        } catch (Exception e) {
            log.error("파일 다운로드 실패: {}", document.getDocumentName(), e);
            throw new IllegalStateException("파일 다운로드에 실패했습니다: " + document.getDocumentName(), e);
        }
    }

    // 팀 드라이브 폴더 이름 변경
    public DriveItemDto renameTeamFolder(Long userId, Long folderId, RenameFolderRequest request) {
        // TODO: 팀 멤버 권한 확인 로직 추가 필요
        
        return commonDriveService.renameFolder(folderId, request.getNewFolderName());
    }

    // 팀 드라이브 아이템 삭제
    public void deleteTeamItem(Long userId, String itemType, Long itemId) {
        // TODO: 팀 멤버 권한 확인 로직 추가 필요

        commonDriveService.deleteItem(itemType, itemId);
    }


    // TODO: 팀 스페이스 생성자가 진행할 예정.
    // 팀 드라이브 채널 생성
//    public DriveItemDto createTeamDriveChannel(Long userId, CreateDriveChannelRequest request) {
//        DriveChannel channel = DriveChannel.builder()
//                .driveChannelName(request.getDriveChannelName())
//                .workspaceSeq(request.getWorkspaceSeq())
//                .workspaceType(WorkSpaceType.TEAM)
//                .build();
//
//        return null;
//    }
}
