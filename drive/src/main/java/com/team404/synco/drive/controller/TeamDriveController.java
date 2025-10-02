package com.team404.synco.drive.controller;

import com.team404.synco.common.dto.CommonDto;
import com.team404.synco.drive.dto.*;
import com.team404.synco.drive.service.TeamDriveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
            @RequestParam(required = false) Long parentFolderId) {

        List<DriveItemDto> items = teamDriveService.getTeamDriveItems(driveChannelSeq, parentFolderId);
        return CommonDto.ok(items, HttpStatus.OK);
    }

    // 팀 드라이브 폴더 생성
    @PostMapping("/folders")
    public CommonDto<?> createTeamFolder(
            @RequestHeader(value = "X-Member-Seq", defaultValue = "1") Long userId,
            @RequestBody CreateFolderRequest request) {
        
        DriveItemDto folder = teamDriveService.createTeamFolder(userId, request);
        return CommonDto.ok(folder, HttpStatus.CREATED);
    }

    // 팀 드라이브 공유문서 생성
    @PostMapping("/shared-docs")
    public CommonDto<?> createTeamSharedDoc(
            @RequestHeader(value = "X-Member-Seq", defaultValue = "1") Long userId,
            @RequestBody CreateSharedDocRequest request) {
        
        DriveItemDto sharedDoc = teamDriveService.createTeamSharedDoc(userId, request);
        return CommonDto.ok(sharedDoc, HttpStatus.CREATED);
    }

    // 팀 드라이브 파일 업로드
    @PostMapping("/upload")
    public CommonDto<?> uploadTeamFiles(
            @RequestHeader(value = "X-Member-Seq", defaultValue = "1") Long userId,
            @ModelAttribute FileUploadRequest request) {

        List<DriveItemDto> uploadedFiles = teamDriveService.uploadTeamFiles(userId, request.getFiles(), request.getDriveChannelSeq(), request.getParentFolderSeq());
        return CommonDto.ok(uploadedFiles, HttpStatus.CREATED);
    }

    // 팀 드라이브 아이템 이동
    @PutMapping("/move")
    public CommonDto<?> moveTeamItem(
            @RequestHeader(value = "X-Member-Seq", defaultValue = "1") Long userId,
            @RequestBody MoveItemRequest request) {
        
        teamDriveService.moveTeamItem(userId, request);
        return CommonDto.ok(null, HttpStatus.NO_CONTENT);
    }

    // 팀 드라이브 파일 다운로드
    @GetMapping("/download/{documentSeq}")
    public ResponseEntity<byte[]> downloadTeamFile(
            @RequestHeader(value = "X-Member-Seq", defaultValue = "1") Long userId,
            @PathVariable Long documentSeq) {
        
        return teamDriveService.downloadTeamFile(userId, documentSeq);
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

    // 팀 드라이브 채널 생성
    @PostMapping("/channels")
    public CommonDto<?> createTeamDriveChannel(
            @RequestHeader(value = "X-Member-Seq", defaultValue = "1") Long userId,
            @RequestBody CreateDriveChannelRequest request) {
        
        DriveItemDto channel = teamDriveService.createTeamDriveChannel(userId, request);
        return CommonDto.ok(channel, HttpStatus.CREATED);
    }
}
