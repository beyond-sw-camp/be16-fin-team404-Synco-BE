package com.team404.synco.drive.controller;

import com.team404.synco.common.dto.CommonDto;
import com.team404.synco.drive.dto.*;
import com.team404.synco.drive.service.PersonalDriveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/drive/personal")
@RequiredArgsConstructor
public class PersonalDriveController {

    private final PersonalDriveService personalDriveService;

    // 개인 드라이브 아이템 목록 조회
    @GetMapping("/items")
    public CommonDto<?> getPersonalDriveItems(
            @RequestHeader(value = "X-Member-Seq", defaultValue = "1") Long userId,
            @RequestParam(required = false) Long parentFolderId,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false, defaultValue = "name") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortOrder) {

        List<DriveItemDto> items = personalDriveService.getPersonalDriveItems(
            userId, parentFolderId, searchQuery, sortBy, sortOrder);
        return CommonDto.ok(items, HttpStatus.OK);
    }

    // 개인 드라이브 폴더 생성
    @PostMapping("/folders")
    public CommonDto<?> createPersonalFolder(
            @RequestHeader(value = "X-Member-Seq", defaultValue = "1") Long userId,
            @RequestBody CreateFolderRequest request) {
        
        DriveItemDto folder = personalDriveService.createPersonalFolder(userId, request);
        return CommonDto.ok(folder, HttpStatus.CREATED);
    }

    // 개인 드라이브 공유문서 생성
    @PostMapping("/shared-docs")
    public CommonDto<?> createPersonalSharedDoc(
            @RequestHeader(value = "X-Member-Seq", defaultValue = "1") Long userId,
            @RequestBody CreateSharedDocRequest request) {
        
        DriveItemDto sharedDoc = personalDriveService.createPersonalSharedDoc(userId, request);
        return CommonDto.ok(sharedDoc, HttpStatus.CREATED);
    }

    // 개인 드라이브 파일 업로드
    @PostMapping("/upload")
    public CommonDto<?> uploadPersonalFiles(
            @RequestHeader("X-User-Id" ) Long userId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(required = false) Long parentFolderId) {

        List<DriveItemDto> uploadedFiles = personalDriveService.uploadPersonalFiles(userId, files, parentFolderId);
        return CommonDto.ok(uploadedFiles, HttpStatus.CREATED);
    }

    // 개인 드라이브 아이템 이동
    @PutMapping("/move")
    public CommonDto<?> movePersonalItem(
            @RequestHeader(value = "X-Member-Seq", defaultValue = "1") Long userId,
            @RequestBody MoveItemRequest request) {
        
        personalDriveService.movePersonalItem(userId, request);
        return CommonDto.ok(null, HttpStatus.NO_CONTENT);
    }

    // 개인 드라이브 파일 다운로드
    @GetMapping("/download/{documentSeq}")
    public ResponseEntity<byte[]> downloadPersonalFile(
            @RequestHeader(value = "X-Member-Seq", defaultValue = "1") Long userId,
            @PathVariable Long documentSeq) {
        
        return personalDriveService.downloadPersonalFile(userId, documentSeq);
    }

    // 개인 드라이브 아이템 삭제
    @DeleteMapping("/{itemType}/{itemId}")
    public CommonDto<?> deletePersonalItem(
            @RequestHeader(value = "X-Member-Seq", defaultValue = "1") Long userId,
            @PathVariable String itemType,
            @PathVariable Long itemId) {
        
        personalDriveService.deletePersonalItem(userId, itemType, itemId);
        return CommonDto.ok(null, HttpStatus.NO_CONTENT);
    }
}
