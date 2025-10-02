package com.team404.synco.drive.controller;

import com.team404.synco.common.dto.CommonDto;
import com.team404.synco.drive.dto.DriveItemDto;
import com.team404.synco.drive.dto.UpdateDocumentRequest;
import com.team404.synco.drive.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    // 공유문서 상세 조회
    @GetMapping("/{documentSeq}")
    public CommonDto<?> getDocument(@PathVariable Long documentSeq) {
        DriveItemDto document = documentService.getDocument(documentSeq);
        return CommonDto.ok(document, HttpStatus.OK);
    }

    // 문서 내용 업데이트
    @PutMapping("/{documentSeq}/content")
    public CommonDto<?> updateDocumentContent(@PathVariable Long documentSeq, @RequestBody UpdateDocumentRequest request) {

        DriveItemDto document = documentService.updateDocumentContent(documentSeq, request);
        return CommonDto.ok(document, HttpStatus.OK);
    }

    // 문서 잠금/잠금해제 토글
    @PatchMapping("/{documentSeq}/lock")
    public CommonDto<?> toggleDocumentLock(@PathVariable Long documentSeq) {
        DriveItemDto document = documentService.toggleDocumentLock(documentSeq);
        return CommonDto.ok(document, HttpStatus.OK);
    }

    // 문서 다운로드
    @GetMapping("/{documentSeq}/download")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long documentSeq) {
        return documentService.downloadDocument(documentSeq);
    }
}
