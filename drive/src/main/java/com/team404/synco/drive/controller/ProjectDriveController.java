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

    // 드라이브 생성
    @PostMapping("/create")
    public ResponseEntity<ResponseDto<?>>createChannel(@RequestBody DriveCreateReqDto driveCreateReqDto){
        Long id = projectDriveService.createChannel(driveCreateReqDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.ok(id, HttpStatus.CREATED));
    }

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
    public ResponseEntity<ResponseDto<?>> createProjectFolder(@RequestBody CreateFolderReqDto createFolderReqDto) {
        
        DriveItemDto folder = projectDriveService.createProjectFolder(createFolderReqDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.ok(folder, HttpStatus.CREATED));
    }

    // 프로젝트 드라이브 공유문서 생성
    @PostMapping("/create/shared-docs")
    public ResponseEntity<ResponseDto<?>> createProjectSharedDoc(
            @RequestHeader(value = "X-Member-Seq") Long userId,
            @RequestBody CreateSharedDocReqDto createSharedDocReqDto) {
        
        DriveItemDto sharedDoc = projectDriveService.createProjectSharedDoc(userId, createSharedDocReqDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.ok(sharedDoc, HttpStatus.CREATED));
    }

    // 프로젝트 드라이브 파일 업로드
    @PostMapping("/upload")
    public ResponseEntity<ResponseDto<?>> uploadProjectFiles(
            @RequestHeader(value = "X-Member-Seq") Long userId,
            @ModelAttribute FileUploadReqDto fileUploadReqDto) {

        List<DriveItemDto> uploadedFiles = projectDriveService.uploadProjectFiles(userId, fileUploadReqDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.ok(uploadedFiles, HttpStatus.CREATED));
    }

    // 프로젝트 드라이브 아이템 이동
    @PatchMapping("/move")
    public ResponseEntity<ResponseDto<?>> moveProjectItem(@RequestBody MoveItemReqDto moveItemReqDto) {
        
        projectDriveService.moveProjectItem(moveItemReqDto);
        return ResponseEntity.ok(ResponseDto.ok("성공적으로 이동하였습니다.", HttpStatus.OK));
    }

    // 프로젝트 드라이브 폴더 순서 변경
    @PatchMapping("/reorder")
    public ResponseEntity<ResponseDto<?>> reorderProjectFolder(@RequestBody ReorderItemReqDto reorderItemReqDto) {
        
        projectDriveService.reorderProjectFolder(reorderItemReqDto);
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
    public ResponseEntity<ResponseDto<?>> renameProjectFolder(@RequestBody RenameFolderReqDto renameFolderReqDto) {
        
        DriveItemDto renamedFolder = projectDriveService.renameProjectFolder(renameFolderReqDto);
        return ResponseEntity.ok(ResponseDto.ok(renamedFolder, HttpStatus.OK));
    }

    // 프로젝트 드라이브 아이템 삭제
    @DeleteMapping("/{driveChannelSeq}")
    public ResponseEntity<ResponseDto<?>> deleteProjectItem(
            @PathVariable Long driveChannelSeq,
            @RequestHeader(value = "X-Member-Seq") Long userId,
            @RequestBody DeleteItemReqDto deleteItemReqDto) {
        
        projectDriveService.deleteProjectItem(driveChannelSeq, userId, deleteItemReqDto.getItemType(), deleteItemReqDto.getItemId());
        return ResponseEntity.ok(ResponseDto.ok("성공적으로 삭제하였습니다.", HttpStatus.OK));
    }



    // 프로젝트 드라이브 공유문서 상세 조회
    @GetMapping("/{driveChannelSeq}/documents/{documentSeq}")
    public ResponseEntity<ResponseDto<?>> getProjectDocument(
            @PathVariable Long driveChannelSeq,
            @PathVariable Long documentSeq) {
        DocumentDetailDto document = projectDriveService.getProjectDocument(driveChannelSeq, documentSeq);
        return ResponseEntity.ok(ResponseDto.ok(document, HttpStatus.OK));
    }

    // 프로젝트 드라이브 공유문서 잠금/해제 토글
    @PostMapping("/documents/lock")
    public ResponseEntity<ResponseDto<?>> toggleProjectDocumentLock(@RequestBody ToggleReqDto toggleReqDto) {
        DriveItemDto document = projectDriveService.toggleProjectDocumentLock(toggleReqDto);
        return ResponseEntity.ok(ResponseDto.ok(document, HttpStatus.OK));
    }

    // 프로젝트 드라이브 공유문서 다운로드
    @GetMapping("/{driveChannelSeq}/documents/{documentSeq}/download")
    public ResponseEntity<byte[]> downloadProjectDocument(
            @PathVariable Long driveChannelSeq,
            @PathVariable Long documentSeq) {
        return projectDriveService.downloadProjectDocument(driveChannelSeq, documentSeq);
    }

    // 프로젝트 드라이브 폴더 트리 조회
    @GetMapping("/{driveChannelSeq}/folders/tree")
    public ResponseEntity<ResponseDto<?>> getProjectFolderTree(@PathVariable Long driveChannelSeq) {
        List<FolderTreeDto> folderTree = projectDriveService.getProjectFolderTree(driveChannelSeq);
        return ResponseEntity.ok(ResponseDto.ok(folderTree, HttpStatus.OK));
    }
}
