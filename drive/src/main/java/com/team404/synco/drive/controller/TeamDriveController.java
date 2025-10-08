package com.team404.synco.drive.controller;

import com.team404.synco.common.dto.CommonDto;
import com.team404.synco.drive.dto.*;
import com.team404.synco.drive.service.TeamDriveService;
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
@RequestMapping("/drive/team")
@RequiredArgsConstructor
public class TeamDriveController {

    private final TeamDriveService teamDriveService;

    // 팀 드라이브 아이템 목록 조회
    @GetMapping("/{driveChannelSeq}/items")
    public CommonDto<?> getTeamDriveItems(
            @PathVariable Long driveChannelSeq,
            @RequestParam(required = false) Long parentFolderId,
            @RequestParam(required = false) String nameFilter,
            @RequestParam(required = false) String modifiedDateFilter,
            @RequestParam(required = false) String byteSizeFilter,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<DriveItemDto> items = teamDriveService.getTeamDriveItems(driveChannelSeq, parentFolderId, pageable, nameFilter, modifiedDateFilter, byteSizeFilter);
        return CommonDto.ok(items, HttpStatus.OK);
    }

    // 팀 드라이브 폴더 생성
    @PostMapping("/folders")
    public CommonDto<?> createTeamFolder(@RequestBody CreateFolderReqDto request) {
        
        DriveItemDto folder = teamDriveService.createTeamFolder(request);
        return CommonDto.ok(folder, HttpStatus.CREATED);
    }

    // 팀 드라이브 공유문서 생성
    @PostMapping("/shared-docs")
    public CommonDto<?> createTeamSharedDoc(
            @RequestHeader(value = "X-Member-Seq", defaultValue = "1") Long userId,
            @RequestBody CreateSharedDocReqDto request) {
        
        DriveItemDto sharedDoc = teamDriveService.createTeamSharedDoc(userId, request);
        return CommonDto.ok(sharedDoc, HttpStatus.CREATED);
    }

    // 팀 드라이브 파일 업로드
    @PostMapping("/upload")
    public CommonDto<?> uploadTeamFiles(
            @RequestHeader(value = "X-Member-Seq", defaultValue = "1") Long userId,
            @ModelAttribute FileUploadReqDto request) {

        List<DriveItemDto> uploadedFiles = teamDriveService.uploadTeamFiles(userId, request.getFiles(), request.getDriveChannelSeq(), request.getParentFolderSeq());
        return CommonDto.ok(uploadedFiles, HttpStatus.CREATED);
    }

    // 팀 드라이브 아이템 이동
    @PatchMapping("/move")
    public CommonDto<?> moveTeamItem(
            @RequestBody MoveItemReqDto request) {
        
        teamDriveService.moveTeamItem(request);
        return CommonDto.ok(null, HttpStatus.NO_CONTENT);
    }

    // 팀 드라이브 아이템 순서 변경
    @PatchMapping("/reorder")
    public CommonDto<?> reorderTeamItem(
            @RequestBody ReorderItemReqDto request) {
        
        teamDriveService.reorderTeamItem(request);
        return CommonDto.ok(null, HttpStatus.NO_CONTENT);
    }

    // 팀 드라이브 파일 다운로드
    @GetMapping("/download/{documentSeq}")
    public ResponseEntity<byte[]> downloadTeamFile(
            @PathVariable Long documentSeq) {
        
        return teamDriveService.downloadTeamFile(documentSeq);
    }

    // 팀 드라이브 폴더 이름 변경
    @PatchMapping("/folders/{folderId}/rename")
    public CommonDto<?> renameTeamFolder(
            @RequestHeader(value = "X-Member-Seq", defaultValue = "1") Long userId,
            @PathVariable Long folderId,
            @RequestBody RenameFolderReqDto request) {
        
        DriveItemDto renamedFolder = teamDriveService.renameTeamFolder(userId, folderId, request);
        return CommonDto.ok(renamedFolder, HttpStatus.OK);
    }

    // 팀 드라이브 아이템 삭제
    @DeleteMapping("/{itemType}/{itemId}")
    public CommonDto<?> deleteTeamItem(
            @RequestHeader(value = "X-Member-Seq", defaultValue = "1") Long userId,
            @PathVariable String itemType,
            @PathVariable Long itemId) {
        
        teamDriveService.deleteTeamItem(userId, itemType, itemId);
        return CommonDto.ok(null, HttpStatus.NO_CONTENT);
    }

    // TODO: 팀 스페이스 생성자가 진행할 예정.
    // 팀 드라이브 채널 생성
//    @PostMapping("/channels")
//    public CommonDto<?> createTeamDriveChannel(
//            @RequestHeader(value = "X-Member-Seq", defaultValue = "1") Long userId,
//            @RequestBody CreateDriveChannelRequest request) {
//        
//        DriveItemDto channel = teamDriveService.createTeamDriveChannel(userId, request);
//        return CommonDto.ok(channel, HttpStatus.CREATED);
//    }
}
