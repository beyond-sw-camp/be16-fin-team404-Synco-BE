package com.team404.synco.drive.controller;

import com.team404.synco.common.dto.CommonDto;
import com.team404.synco.drive.dto.*;
import com.team404.synco.drive.service.DriveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/drive")
@RequiredArgsConstructor
public class DriveController {

    private final DriveService driveService;

    // 드라이브 아이템(폴더 및 파일) 조회
    @GetMapping("/{driveChannelSeq}/items")
    public ResponseEntity<?> getDriveItems(
            @PathVariable Long driveChannelSeq,
            @RequestParam(required = false) Long parentFolderId,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false, defaultValue = "name") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortOrder) {

        List<DriveItemDto> items = driveService.getDriveItems(driveChannelSeq, parentFolderId, searchQuery, sortBy, sortOrder);
        return new ResponseEntity<>(CommonDto.ok(items, HttpStatus.OK), HttpStatus.OK);
    }


    // 폴더 생성
    @PostMapping("/folder/create")
    public ResponseEntity<?> createFolder(@RequestBody CreateFolderRequest request) {
        DriveItemDto folder = driveService.createFolder(request);
        return new ResponseEntity<>(CommonDto.ok(folder, HttpStatus.CREATED), HttpStatus.CREATED);
    }

    // 공유 문서 생성
    @PostMapping("/shared-docs")
    public ResponseEntity<?> createSharedDoc(
            @RequestBody CreateSharedDocRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        DriveItemDto sharedDoc = driveService.createSharedDoc(request, userId);
        return new ResponseEntity<>(CommonDto.ok(sharedDoc, HttpStatus.CREATED), HttpStatus.CREATED);
    }

    // 아이템(문서, 폴더) 삭제
    @DeleteMapping("/{itemType}/{itemId}")
    public ResponseEntity<?> deleteItem(
            @PathVariable String itemType,
            @PathVariable Long itemId) {
        driveService.deleteItem(itemType, itemId);
        return new ResponseEntity<>(CommonDto.ok(null, HttpStatus.NO_CONTENT), HttpStatus.NO_CONTENT);
    }

    // 파일 업로드
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(
            @ModelAttribute FileUploadRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        List<DriveItemDto> uploadedFiles = driveService.uploadFiles(request, userId);
        return new ResponseEntity<>(CommonDto.ok(uploadedFiles, HttpStatus.CREATED), HttpStatus.CREATED);
    }

    // 아이템(문서, 폴더) 이동
    @PutMapping("/move")
    public ResponseEntity<?> moveItem(
            @RequestBody MoveItemRequest request) {
        driveService.moveItem(request);
        return new ResponseEntity<>(CommonDto.ok(null, HttpStatus.NO_CONTENT), HttpStatus.NO_CONTENT);
    }

    // 파일 다운로드
    @GetMapping("/download/{documentSeq}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long documentSeq) {
        return driveService.downloadFile(documentSeq);
    }


}
