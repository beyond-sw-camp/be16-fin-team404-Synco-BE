package com.team404.synco.drive.controller;

import com.team404.synco.common.dto.ResponseDto;
import com.team404.synco.drive.dto.*;
import com.team404.synco.drive.service.ProjectDriveService;
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
@RequestMapping("/drive/project")
@RequiredArgsConstructor
public class ProjectDriveController {

    private final ProjectDriveService projectDriveService;

    // 프로젝트 드라이브 아이템 목록 조회
    @GetMapping("/{driveChannelSeq}/items")
    public ResponseEntity<ResponseDto<?>> getProjectDriveItems(
            @PathVariable Long driveChannelSeq,
            @RequestParam(required = false) Long parentFolderId,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<DriveItemDto> items = projectDriveService.getProjectDriveItems(driveChannelSeq, parentFolderId, pageable, sortBy, sortOrder);
        return ResponseEntity.ok(ResponseDto.ok(items, HttpStatus.OK));
    }

    // 프로젝트 드라이브 폴더 생성
    @PostMapping("/folder")
    public ResponseEntity<ResponseDto<?>> createProjectFolder(@RequestBody CreateFolderReqDto request) {
        
        DriveItemDto folder = projectDriveService.createProjectFolder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.ok(folder, HttpStatus.CREATED));
    }

    // 프로젝트 드라이브 공유문서 생성
    @PostMapping("/create/shared-docs")
    public ResponseEntity<ResponseDto<?>> createProjectSharedDoc(
            @RequestHeader(value = "X-Member-Seq") Long userId,
            @RequestBody CreateSharedDocReqDto request) {
        
        DriveItemDto sharedDoc = projectDriveService.createProjectSharedDoc(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.ok(sharedDoc, HttpStatus.CREATED));
    }

    // 프로젝트 드라이브 파일 업로드
    @PostMapping("/upload")
    public ResponseEntity<ResponseDto<?>> uploadProjectFiles(
            @RequestHeader(value = "X-Member-Seq") Long userId,
            @ModelAttribute FileUploadReqDto request) {

        List<DriveItemDto> uploadedFiles = projectDriveService.uploadProjectFiles(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.ok(uploadedFiles, HttpStatus.CREATED));
    }

    // 프로젝트 드라이브 아이템 이동
    @PatchMapping("/move")
    public ResponseEntity<ResponseDto<?>> moveProjectItem(@RequestBody MoveItemReqDto request) {
        
        projectDriveService.moveProjectItem(request);
        return ResponseEntity.ok(ResponseDto.ok("성공적으로 이동하였습니다.", HttpStatus.OK));
    }

    // 프로젝트 드라이브 폴더 순서 변경
    @PatchMapping("/reorder")
    public ResponseEntity<ResponseDto<?>> reorderProjectFolder(@RequestBody ReorderItemReqDto request) {
        
        projectDriveService.reorderProjectFolder(request);
        return ResponseEntity.ok(ResponseDto.ok("성공적으로 순서를 변경하였습니다.", HttpStatus.OK));
    }

    // 프로젝트 드라이브 파일 다운로드
    @GetMapping("/{driveChannelSeq}/download/{documentSeq}")
    public ResponseEntity<byte[]> downloadProjectFile(
            @PathVariable Long driveChannelSeq,
            @PathVariable Long documentSeq) {
        
        return projectDriveService.downloadProjectFile(driveChannelSeq, documentSeq);
    }

    // 프로젝트 드라이브 폴더 이름 변경
    @PatchMapping("/folder/rename")
    public ResponseEntity<ResponseDto<?>> renameProjectFolder(@RequestBody RenameFolderReqDto request) {
        
        DriveItemDto renamedFolder = projectDriveService.renameProjectFolder(request);
        return ResponseEntity.ok(ResponseDto.ok(renamedFolder, HttpStatus.OK));
    }

    // 프로젝트 드라이브 아이템 삭제
    @DeleteMapping("/{driveChannelSeq}/delete")
    public ResponseEntity<ResponseDto<?>> deleteProjectItem(
            @PathVariable Long driveChannelSeq,
            @RequestHeader(value = "X-Member-Seq") Long userId,
            @RequestBody DeleteItemReqDto request) {
        
        projectDriveService.deleteProjectItem(driveChannelSeq, userId, request.getItemType(), request.getItemId());
        return ResponseEntity.ok(ResponseDto.ok("성공적으로 삭제하였습니다.", HttpStatus.OK));
    }

    // 프로젝트 드라이브 문서 상세 조회
    @GetMapping("/{driveChannelSeq}/documents/{documentSeq}")
    public ResponseEntity<ResponseDto<?>> getProjectDocument(
            @PathVariable Long driveChannelSeq,
            @PathVariable Long documentSeq) {
        DocumentDetailDto document = projectDriveService.getProjectDocument(driveChannelSeq, documentSeq);
        return ResponseEntity.ok(ResponseDto.ok(document, HttpStatus.OK));
    }

//    // 프로젝트 드라이브 문서 내용 업데이트
//    // TODO: 추후 개발 예정
//    @PutMapping("/documents/update")
//    public CommonDto<?> updateProjectDocumentContent(@RequestBody UpdateDocumentReqDto request) {
//        DriveItemDto updatedDocument = projectDriveService.updateProjectDocumentContent(request);
//        return CommonDto.ok(updatedDocument, HttpStatus.OK);
//    }

    // 프로젝트 드라이브 문서 잠금/해제 토글
    @PostMapping("/documents/lock")
    public ResponseEntity<ResponseDto<?>> toggleProjectDocumentLock(@RequestBody ToggleReqDto request) {
        DriveItemDto document = projectDriveService.toggleProjectDocumentLock(request);
        return ResponseEntity.ok(ResponseDto.ok(document, HttpStatus.OK));
    }

    // 프로젝트 드라이브 문서 다운로드
    @GetMapping("/{driveChannelSeq}/documents/{documentSeq}/download")
    public ResponseEntity<byte[]> downloadProjectDocument(
            @PathVariable Long driveChannelSeq,
            @PathVariable Long documentSeq) {
        return projectDriveService.downloadProjectDocument(driveChannelSeq, documentSeq);
    }

    // TODO: 프로젝트 스페이스 생성자가 진행할 예정.
    // 프로젝트 드라이브 채널 생성
//    @PostMapping("/channels")
//    public CommonDto<?> createProjectDriveChannel(
//            @RequestHeader(value = "X-Member-Seq", defaultValue = "1") Long userId,
//            @RequestBody CreateDriveChannelRequest request) {
//        
//        DriveItemDto channel = projectDriveService.createProjectDriveChannel(userId, request);
//        return CommonDto.ok(channel, HttpStatus.CREATED);
//    }
}
