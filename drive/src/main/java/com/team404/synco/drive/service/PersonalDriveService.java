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
public class PersonalDriveService {

    private final CommonDriveService commonDriveService;
    private final DocumentRepository documentRepository;

    // 개인 드라이브 아이템 목록 조회
    public List<DriveItemDto> getPersonalDriveItems(Long userId, Long parentFolderId, 
                                                   String searchQuery, String sortBy, String sortOrder) {
        // 사용자의 개인 드라이브 채널 조회
        DriveChannel personalDrive = commonDriveService.getPersonalDriveChannel(userId);
        
        // 공통 서비스 사용
        return commonDriveService.getDriveItems(personalDrive, parentFolderId, searchQuery, sortBy, sortOrder);
    }

    // 개인 드라이브 폴더 생성
    public DriveItemDto createPersonalFolder(Long userId, CreateFolderRequest request) {
        // 사용자의 개인 드라이브 채널 조회
        DriveChannel personalDrive = commonDriveService.getPersonalDriveChannel(userId);
        
        // 공통 서비스 사용
        return commonDriveService.createFolder(personalDrive, request.getFolderName(), request.getParentFolderId());
    }

    // 개인 드라이브 공유문서 생성
    public DriveItemDto createPersonalSharedDoc(Long userId, CreateSharedDocRequest request) {
        // 사용자의 개인 드라이브 채널 조회
        DriveChannel personalDrive = commonDriveService.getPersonalDriveChannel(userId);
        
        // 개인 드라이브에서는 잠금 기능 불필요 (본인만 접근)
        return commonDriveService.createSharedDoc(personalDrive, userId, request.getDocumentName(), 
            request.getParentFolderId(), false, request.getContent());
    }

    // 개인 드라이브 파일 업로드
    public List<DriveItemDto> uploadPersonalFiles(Long userId, List<MultipartFile> files, Long parentFolderId) {
        // 사용자의 개인 드라이브 채널 조회
        DriveChannel personalDrive = commonDriveService.getPersonalDriveChannel(userId);
        
        // 공통 서비스 사용
        return commonDriveService.uploadFiles(personalDrive, userId, files, parentFolderId);
    }

    // 개인 드라이브 아이템 이동
    public void movePersonalItem(Long userId, MoveItemRequest request) {
        // 개인 드라이브 권한 확인 (본인만 접근 가능)
        validatePersonalDriveAccess(userId, request.getItemType(), request.getItemId());
        
        // 공통 서비스 사용
        commonDriveService.moveItem(request.getItemType(), request.getItemId(), request.getNewParentId());
    }

    // 개인 드라이브 파일 다운로드
    public ResponseEntity<byte[]> downloadPersonalFile(Long userId, Long documentSeq) {
        // 개인 드라이브 권한 확인
        validatePersonalDocumentAccess(userId, documentSeq);
        
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

    // 개인 드라이브 아이템 삭제
    public void deletePersonalItem(Long userId, String itemType, Long itemId) {
        // 개인 드라이브 권한 확인
        validatePersonalDriveAccess(userId, itemType, itemId);
        
        // 공통 서비스 사용
        commonDriveService.deleteItem(itemType, itemId);
    }

    // 개인 드라이브 전용 권한 확인 메서드들
    private void validatePersonalDriveAccess(Long userId, String itemType, Long itemId) {
        // TODO: 개인 드라이브 소유자인지 확인하는 로직 구현
        // 현재는 간단히 userId만 확인
        log.debug("개인 드라이브 권한 확인: userId={}, itemType={}, itemId={}", userId, itemType, itemId);
    }

    private void validatePersonalDocumentAccess(Long userId, Long documentSeq) {
        // TODO: 개인 드라이브 문서 소유자인지 확인하는 로직 구현
        log.debug("개인 드라이브 문서 권한 확인: userId={}, documentSeq={}", userId, documentSeq);
    }
}
