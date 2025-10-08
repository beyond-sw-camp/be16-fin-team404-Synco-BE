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

    // 개인 드라이브 아이템 목록 조회
    public Page<DriveItemDto> getPersonalDriveItems(Long driveChannelSeq, Long parentFolderId, Pageable pageable, String nameFilter, String modifiedDateFilter, String byteSizeFilter) {
        DriveChannel personalDrive = commonDriveService.getDriveChannel(driveChannelSeq);
        return commonDriveService.getDriveItems(personalDrive, parentFolderId, pageable, nameFilter, modifiedDateFilter, byteSizeFilter);
    }

    // 개인 드라이브 폴더 생성
    public DriveItemDto createPersonalFolder(CreateFolderReqDto request) {
        DriveChannel personalDrive = commonDriveService.getDriveChannel(request.getDriveChannelSeq());
        return commonDriveService.createFolder(personalDrive, request.getFolderName(), request.getParentFolderSeq());
    }

    // 개인 드라이브 공유문서 생성
    public DriveItemDto createPersonalSharedDoc(Long userId, CreateSharedDocReqDto request) {
        commonDriveService.getDriveChannel(request.getDriveChannelSeq());
        return commonDriveService.createSharedDoc(userId, request.getDocumentName(),
                request.getParentFolderSeq(), true);
    }

    // 개인 드라이브 파일 업로드
    public List<DriveItemDto> uploadPersonalFiles(Long userId, FileUploadReqDto request) {
        DriveChannel personalDrive = commonDriveService.getDriveChannel(userId);
        return commonDriveService.uploadFiles(personalDrive, userId, request.getFiles(), request.getParentFolderSeq());
    }

    // 개인 드라이브 아이템 이동
    public void movePersonalItem(MoveItemReqDto request) {
        commonDriveService.moveItem(request.getItemType(), request.getItemId(), request.getNewParentSeq());
    }

    // 개인 드라이브 아이템 순서 변경
    public void reorderPersonalItem(ReorderItemReqDto request) {
        commonDriveService.reorderItem(request.getItemType(), request.getItemId(), request.getNewOrder());
    }

    // 개인 드라이브 파일 다운로드
    public ResponseEntity<byte[]> downloadPersonalFile(Long userId, Long documentSeq) {

        Document document = documentRepository.findById(documentSeq)
            .orElseThrow(() -> new EntityNotFoundException("파일을 찾을 수 없습니다."));
        
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

    // 개인 드라이브 폴더 이름 변경
    public DriveItemDto renamePersonalFolder(Long folderId, RenameFolderReqDto request) {
        return commonDriveService.renameFolder(folderId, request.getNewFolderName());
    }

    // 개인 드라이브 아이템 삭제
    public void deletePersonalItem(Long userId, String itemType, Long itemId) {
        commonDriveService.deleteItem(userId, itemType, itemId);
    }
}
