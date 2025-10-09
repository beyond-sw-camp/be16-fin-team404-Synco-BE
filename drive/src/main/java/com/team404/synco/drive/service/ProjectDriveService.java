package com.team404.synco.drive.service;

import com.team404.synco.drive.dto.*;
import com.team404.synco.drive.entity.Document;
import com.team404.synco.drive.entity.DriveChannel;
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProjectDriveService {

    private final CommonDriveService commonDriveService;
    private final DocumentRepository documentRepository;

    // 프로젝트 드라이브 아이템 목록 조회
    public Page<DriveItemDto> getProjectDriveItems(Long driveChannelSeq, Long parentFolderId, Pageable pageable, String sortBy, String sortOrder) {
        DriveChannel driveChannel = commonDriveService.getProjectDriveChannel(driveChannelSeq);
        return commonDriveService.getDriveItems(driveChannel, parentFolderId, pageable, sortBy, sortOrder);
    }

    // 프로젝트 드라이브 폴더 생성
    public DriveItemDto createProjectFolder(CreateFolderReqDto request) {
        DriveChannel driveChannel = commonDriveService.getProjectDriveChannel(request.getDriveChannelSeq());
        return commonDriveService.createFolder(driveChannel, request.getFolderName(), request.getParentFolderSeq());
    }

    // 프로젝트 드라이브 공유문서 생성
    public DriveItemDto createProjectSharedDoc(Long userId, CreateSharedDocReqDto request) {
        DriveChannel projectDriveChannel = commonDriveService.getProjectDriveChannel(request.getDriveChannelSeq());
        return commonDriveService.createSharedDoc(projectDriveChannel,userId, request.getDocumentName(), request.getParentFolderSeq(), request.getIsLocked());
    }

    // 프로젝트 드라이브 파일 업로드
    public List<DriveItemDto> uploadProjectFiles(Long userId, List<MultipartFile> files, Long driveChannelSeq, Long parentFolderId) {
        DriveChannel driveChannel = commonDriveService.getProjectDriveChannel(driveChannelSeq);
        return commonDriveService.uploadFiles(driveChannel, userId, files, parentFolderId);
    }

    // 프로젝트 드라이브 아이템 이동
    public void moveProjectItem(MoveItemReqDto request) {
        commonDriveService.moveItem(request.getItemType(), request.getItemId(), request.getNewParentSeq());
    }

    // 프로젝트 드라이브 아이템 순서 변경
    public void reorderProjectItem(ReorderItemReqDto request) {
        commonDriveService.reorderItem(request.getItemType(), request.getItemId(), request.getNewOrder());
    }

    // 프로젝트 드라이브 파일 다운로드
    public ResponseEntity<byte[]> downloadProjectFile(Long documentSeq) {
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

    // 프로젝트 드라이브 폴더 이름 변경
    public DriveItemDto renameProjectFolder(Long folderId, RenameFolderReqDto request) {
        return commonDriveService.renameFolder(folderId, request.getNewFolderName());
    }

    // 프로젝트 드라이브 아이템 삭제
    public void deleteProjectItem(Long userId, String itemType, Long itemId) {
        commonDriveService.deleteItem(userId, itemType, itemId);
    }


    // TODO: 프로젝트 스페이스 생성자가 진행할 예정.
    // 프로젝트 드라이브 채널 생성
//    public DriveItemDto createProjectDriveChannel(Long userId, CreateDriveChannelRequest request) {
//        DriveChannel channel = DriveChannel.builder()
//                .driveChannelName(request.getDriveChannelName())
//                .workspaceSeq(request.getWorkspaceSeq())
//                .workspaceType(WorkSpaceType.PROJECT)
//                .build();
//
//        return null;
//    }
}
