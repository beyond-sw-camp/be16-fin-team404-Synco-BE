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
        return CommonDto.ok(null, HttpStatus.OK);
    }

    // 개인 드라이브 폴더 순서 변경
    @PatchMapping("/reorder")
    public CommonDto<?> reorderPersonalFolder(@RequestBody ReorderItemReqDto request) {
        
        personalDriveService.reorderPersonalFolder(request);
        return CommonDto.ok(null, HttpStatus.OK);
    }

    // 개인 드라이브 파일 다운로드
    @GetMapping("/{driveChannelSeq}/download/{documentSeq}")
    public ResponseEntity<byte[]> downloadPersonalFile(
            @PathVariable Long driveChannelSeq,
            @PathVariable Long documentSeq) {
        return personalDriveService.downloadPersonalFile(driveChannelSeq, documentSeq);
    }

    // 폴더 이름 변경
    @PatchMapping("/folder/rename")
    public CommonDto<?> renamePersonalFolder(@RequestBody RenameFolderReqDto request) {
        DriveItemDto renamedFolder = personalDriveService.renamePersonalFolder(request);
        return CommonDto.ok(renamedFolder, HttpStatus.OK);
    }

    // 개인 드라이브 아이템 삭제
    @DeleteMapping("/{driveChannelSeq}/delete")
    public CommonDto<?> deletePersonalItem(
            @PathVariable Long driveChannelSeq,
            @RequestHeader(value = "X-Member-Seq") Long userId,
            @RequestBody DeleteItemReqDto request) {
        
        personalDriveService.deletePersonalItem(driveChannelSeq, userId, request.getItemType(), request.getItemId());
        return CommonDto.ok("성공적으로 삭제하였습니다.", HttpStatus.OK);
    }

    // 개인 드라이브 문서 상세 조회
    @GetMapping("/{driveChannelSeq}/documents/{documentSeq}")
    public CommonDto<?> getPersonalDocument(
            @PathVariable Long driveChannelSeq,
            @PathVariable Long documentSeq) {
        DocumentDetailDto document = personalDriveService.getPersonalDocument(driveChannelSeq, documentSeq);
        return CommonDto.ok(document, HttpStatus.OK);
    }

//    // 개인 드라이브 문서 내용 업데이트
//    // TODO: 추후 개발 예정
//    @PutMapping("/documents/update")
//    public CommonDto<?> updatePersonalDocumentContent(@RequestBody DriveItemDto request) {
//        DriveItemDto updatedDocument = personalDriveService.updatePersonalDocumentContent(request);
//        return CommonDto.ok(updatedDocument, HttpStatus.OK);
//    }

    // 개인 드라이브 문서 잠금/해제 토글
    @PostMapping("/documents/lock")
    public CommonDto<?> togglePersonalDocumentLock(@RequestBody ToggleReqDto request) {
        DriveItemDto document = personalDriveService.togglePersonalDocumentLock(request);
        return CommonDto.ok(document, HttpStatus.OK);
    }

    // 개인 드라이브 문서 다운로드
    @GetMapping("/{driveChannelSeq}/documents/{documentSeq}/download")
    public ResponseEntity<byte[]> downloadPersonalDocument(
            @PathVariable Long driveChannelSeq,
            @PathVariable Long documentSeq) {
        return personalDriveService.downloadPersonalDocument(driveChannelSeq, documentSeq);
    }
}
