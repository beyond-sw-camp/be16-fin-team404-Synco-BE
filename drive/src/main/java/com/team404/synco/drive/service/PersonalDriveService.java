package com.team404.synco.drive.service;

import com.team404.synco.drive.dto.*;
import com.team404.synco.drive.entity.DriveChannel;
import com.team404.synco.drive.entity.Document;
import com.team404.synco.drive.repository.DocumentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PersonalDriveService {

    private final CommonDriveService commonDriveService;
    private final DocumentRepository documentRepository;

    // 개인 드라이브 아이템 목록 조회 (페이지네이션)
    public Page<DriveItemDto> getPersonalDriveItems(Long driveChannelSeq, Long parentFolderId, Pageable pageable) {
        DriveChannel personalDrive = commonDriveService.getDriveChannel(driveChannelSeq);
        return commonDriveService.getDriveItems(personalDrive, parentFolderId, pageable);
    }

    // 개인 드라이브 폴더 생성
    public DriveItemDto createPersonalFolder(CreateFolderRequest request) {
        DriveChannel personalDrive = commonDriveService.getDriveChannel(request.getDriveChannelSeq());
        return commonDriveService.createFolder(personalDrive, request.getFolderName(), request.getParentFolderSeq());
    }

    // 개인 드라이브 공유문서 생성
    public DriveItemDto createPersonalSharedDoc(Long userId,CreateSharedDocRequest request) {
        commonDriveService.getDriveChannel(request.getDriveChannelSeq());
        return commonDriveService.createSharedDoc(userId, request.getDocumentName(),
                request.getParentFolderSeq(), true);
    }

    // 개인 드라이브 파일 업로드
    public List<DriveItemDto> uploadPersonalFiles(Long userId, FileUploadRequest request) {
        DriveChannel personalDrive = commonDriveService.getDriveChannel(userId);
        return commonDriveService.uploadFiles(personalDrive, userId, request.getFiles(), request.getParentFolderSeq());
    }

    // 개인 드라이브 아이템 이동
    public void movePersonalItem(MoveItemRequest request) {
        commonDriveService.moveItem(request.getItemType(), request.getItemId(), request.getNewParentSeq());
    }

    // 개인 드라이브 아이템 순서 변경
    public void reorderPersonalItem(ReorderItemRequest request) {
        commonDriveService.reorderItem(request.getItemType(), request.getItemId(), request.getNewOrder());
    }

    // 개인 드라이브 파일 다운로드
    public ResponseEntity<byte[]> downloadPersonalFile(Long userId, Long documentSeq) {

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

    // 개인 드라이브 폴더 이름 변경
    public DriveItemDto renamePersonalFolder(Long folderId, RenameFolderRequest request) {
        return commonDriveService.renameFolder(folderId, request.getNewFolderName());
    }

    // 개인 드라이브 아이템 삭제
    public void deletePersonalItem(String itemType, Long itemId) {
        commonDriveService.deleteItem(itemType, itemId);
    }
}
