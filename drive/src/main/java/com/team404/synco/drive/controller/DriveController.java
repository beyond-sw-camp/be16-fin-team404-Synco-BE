package com.team404.synco.drive.controller;

import com.team404.synco.common.dto.CommonDto;
import com.team404.synco.drive.dto.*;
import com.team404.synco.drive.service.DriveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/drive")
@RequiredArgsConstructor
public class DriveController {

    private final DriveService driveService;

    /**
     * 드라이브 채널의 파일/폴더 목록 조회 (계층 구조)
     */
    @GetMapping("/{driveChannelSeq}/items")
    public ResponseEntity<CommonDto<List<DriveItemDto>>> getDriveItems(
            @PathVariable Long driveChannelSeq,
            @RequestParam(required = false) Long parentFolderId,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false, defaultValue = "name") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortOrder) {

        List<DriveItemDto> items = driveService.getDriveItems(driveChannelSeq, parentFolderId, searchQuery, sortBy, sortOrder);
        return new ResponseEntity<>(CommonDto.ok(items, HttpStatus.OK), HttpStatus.OK);
    }

    /**
     * 폴더 생성
     */
    @PostMapping("/folders")
    public ResponseEntity<CommonDto<DriveItemDto>> createFolder(@RequestBody CreateFolderRequest request) {
        DriveItemDto folder = driveService.createFolder(request);
        return new ResponseEntity<>(CommonDto.ok(folder, HttpStatus.CREATED), HttpStatus.CREATED);
    }

    /**
     * 공유문서 생성
     */
    @PostMapping("/shared-docs")
    public ResponseEntity<CommonDto<DriveItemDto>> createSharedDoc(@RequestBody CreateSharedDocRequest request) {
        DriveItemDto sharedDoc = driveService.createSharedDoc(request);
        return new ResponseEntity<>(CommonDto.ok(sharedDoc, HttpStatus.CREATED), HttpStatus.CREATED);
    }

    /**
     * 파일 업로드
     */
    @PostMapping("/upload")
    public ResponseEntity<CommonDto<List<DriveItemDto>>> uploadFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam Long driveChannelSeq,
            @RequestParam(required = false) Long parentFolderId) {

        List<DriveItemDto> uploadedFiles = driveService.uploadFiles(files, driveChannelSeq, parentFolderId);
        return new ResponseEntity<>(CommonDto.ok(uploadedFiles, HttpStatus.CREATED), HttpStatus.CREATED);
    }

    /**
     * 파일/폴더 이동
     */
    @PutMapping("/move")
    public ResponseEntity<CommonDto<Void>> moveItem(
            @RequestBody MoveItemRequest request) {
        driveService.moveItem(request);
        return new ResponseEntity<>(CommonDto.ok(null, HttpStatus.NO_CONTENT), HttpStatus.NO_CONTENT);
    }

    /**
     * 파일 다운로드
     */
    @GetMapping("/download/{documentSeq}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long documentSeq) {
        return driveService.downloadFile(documentSeq);
    }

    /**
     * 폴더/문서 삭제
     */
    @DeleteMapping("/{itemType}/{itemId}")
    public ResponseEntity<CommonDto<Void>> deleteItem(
            @PathVariable String itemType,
            @PathVariable Long itemId) {
        driveService.deleteItem(itemType, itemId);
        return new ResponseEntity<>(CommonDto.ok(null, HttpStatus.NO_CONTENT), HttpStatus.NO_CONTENT);
    }

    /**
     * 드라이브 채널 생성
     */
    @PostMapping("/channels")
    public ResponseEntity<CommonDto<DriveItemDto>> createDriveChannel(
            @RequestParam String driveChannelName,
            @RequestParam Long workspaceSeq) {
        DriveItemDto channel = driveService.createDriveChannel(driveChannelName, workspaceSeq);
        return new ResponseEntity<>(CommonDto.ok(channel, HttpStatus.CREATED), HttpStatus.CREATED);
    }
}
