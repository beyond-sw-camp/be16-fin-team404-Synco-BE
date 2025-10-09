package com.team404.synco.drive.controller;

import com.team404.synco.common.dto.CommonDto;
import com.team404.synco.drive.dto.*;
import com.team404.synco.drive.service.PersonalDriveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/drive/personal")
@RequiredArgsConstructor
public class PersonalDriveController {

    private final PersonalDriveService personalDriveService;

    // 개인 드라이브 아이템 목록 조회
    @GetMapping("/{driveChannelSeq}/items")
    public CommonDto<?> getPersonalDriveItems(
            @PathVariable Long driveChannelSeq,
            @RequestParam(required = false) Long parentFolderId,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<DriveItemDto> items = personalDriveService.getPersonalDriveItems(driveChannelSeq, parentFolderId, pageable, sortBy, sortOrder);
        return CommonDto.ok(items, HttpStatus.OK);
    }

    // 개인 드라이브 폴더 생성
    @PostMapping("/folder")
    public CommonDto<?> createPersonalFolder(@RequestBody CreateFolderReqDto request) {
        DriveItemDto folder = personalDriveService.createPersonalFolder(request);
        return CommonDto.ok(folder, HttpStatus.CREATED);
    }

    // 개인 드라이브 공유문서 생성
    @PostMapping("/create/shared-docs")
    public CommonDto<?> createPersonalSharedDoc(
            @RequestHeader(value = "X-Member-Seq") Long userId,
            @RequestBody CreateSharedDocReqDto request) {
        
        DriveItemDto sharedDoc = personalDriveService.createPersonalSharedDoc(userId, request);
        return CommonDto.ok(sharedDoc, HttpStatus.CREATED);
    }

    // 개인 드라이브 파일 업로드
    @PostMapping("/upload")
    public CommonDto<?> uploadPersonalFiles(
            @RequestHeader(value = "X-Member-Seq") Long userId,
            @ModelAttribute FileUploadReqDto request) {

        List<DriveItemDto> uploadedFiles = personalDriveService.uploadPersonalFiles(userId, request);
        return CommonDto.ok(uploadedFiles, HttpStatus.CREATED);
    }

    // 개인 드라이브 아이템 이동
    @PatchMapping("/move")
    public CommonDto<?> movePersonalItem(
            @RequestBody MoveItemReqDto request) {
        
        personalDriveService.movePersonalItem(request);
        return CommonDto.ok(null, HttpStatus.NO_CONTENT);
    }

    // 개인 드라이브 아이템 순서 변경
    @PatchMapping("/reorder")
    public CommonDto<?> reorderPersonalItem(@RequestBody ReorderItemReqDto request) {
        
        personalDriveService.reorderPersonalItem(request);
        return CommonDto.ok(null, HttpStatus.NO_CONTENT);
    }

    // 개인 드라이브 파일 다운로드
    @GetMapping("/download/{documentSeq}")
    public ResponseEntity<byte[]> downloadPersonalFile(@PathVariable Long documentSeq) {
        return personalDriveService.downloadPersonalFile(documentSeq);
    }

    // 폴더 이름 변경
    @PatchMapping("/folder/{folderId}/rename")
    public CommonDto<?> renamePersonalFolder(
            @PathVariable Long folderId,
            @RequestBody RenameFolderReqDto request) {
        DriveItemDto renamedFolder = personalDriveService.renamePersonalFolder(folderId, request);
        return CommonDto.ok(renamedFolder, HttpStatus.OK);
    }

    // 개인 드라이브 아이템 삭제
    @DeleteMapping("/{itemType}/{itemId}")
    public CommonDto<?> deletePersonalItem(
            @RequestHeader(value = "X-Member-Seq") Long userId,
            @PathVariable String itemType,
            @PathVariable Long itemId) {
        
        personalDriveService.deletePersonalItem(userId, itemType, itemId);
        return CommonDto.ok("성공적으로 삭제하였습니다.", HttpStatus.NO_CONTENT);
    }
}
