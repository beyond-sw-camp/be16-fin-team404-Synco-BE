package com.team404.synco.drive.service;

import com.team404.synco.common.constant.WorkSpaceType;
import com.team404.synco.common.constant.YnColumn;
import com.team404.synco.drive.dto.*;
import com.team404.synco.drive.entity.Document;
import com.team404.synco.drive.entity.DocumentLine;
import com.team404.synco.drive.entity.DriveChannel;
import com.team404.synco.drive.repository.DocumentLineRepository;
import com.team404.synco.drive.repository.DocumentRepository;
import com.team404.synco.drive.repository.DriveChannelRepository;
import com.team404.synco.common.service.S3Uploader;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProjectDriveService {

    private final CommonDriveService commonDriveService;
    private final DocumentRepository documentRepository;
    private final DocumentLineRepository documentLineRepository;
    private final DriveChannelRepository driveChannelRepository;
    private final S3Uploader s3Uploader;

    // 프로젝트 드라이브 채널 조회
    @Transactional(readOnly = true)
    public DriveChannel getProjectDriveChannel(Long driveChannelSeq) {
        DriveChannel channel = driveChannelRepository.findById(driveChannelSeq).orElseThrow(() -> new EntityNotFoundException("드라이브 채널을 찾을 수 없습니다: " + driveChannelSeq));
        
        // 프로젝트 드라이브 채널인지 확인
        if (channel.getWorkspaceType() != WorkSpaceType.PROJECT) {
            throw new IllegalArgumentException("프로젝트 드라이브 채널이 아닙니다: " + driveChannelSeq);
        }
        
        return channel;
    }

    // 프로젝트 드라이브 아이템 목록 조회
    @Transactional(readOnly = true)
    public Page<DriveItemDto> getProjectDriveItems(Long driveChannelSeq, Long parentFolderId, Pageable pageable, String sortBy, String sortOrder) {
        DriveChannel driveChannel = getProjectDriveChannel(driveChannelSeq);
        return commonDriveService.getDriveItems(driveChannel, parentFolderId, pageable, sortBy, sortOrder);
    }

    // 프로젝트 드라이브 폴더 생성
    public DriveItemDto createProjectFolder(CreateFolderReqDto request) {
        DriveChannel driveChannel = getProjectDriveChannel(request.getDriveChannelSeq());
        return commonDriveService.createFolder(driveChannel, request.getFolderName(), request.getParentFolderSeq());
    }

    // 프로젝트 드라이브 공유문서 생성
    public DriveItemDto createProjectSharedDoc(Long userId, CreateSharedDocReqDto request) {
        DriveChannel driveChannel = getProjectDriveChannel(request.getDriveChannelSeq());
        return commonDriveService.createSharedDoc(driveChannel,userId, request.getDocumentName(), request.getParentFolderSeq(), request.getIsLocked());
    }

    // 프로젝트 드라이브 파일 업로드
    public List<DriveItemDto> uploadProjectFiles(Long userId, FileUploadReqDto request) {
        DriveChannel driveChannel = getProjectDriveChannel(request.getDriveChannelSeq());
        return commonDriveService.uploadFiles(driveChannel, userId, request.getFiles(), request.getParentFolderSeq());
    }

    // 프로젝트 드라이브 아이템 이동
    public void moveProjectItem(MoveItemReqDto request) {
        DriveChannel driveChannel = getProjectDriveChannel(request.getDriveChannelSeq());
        commonDriveService.moveItem(driveChannel, request.getItemType(), request.getItemId(), request.getNewParentSeq());
    }

    // 프로젝트 드라이브 폴더 순서 변경
    public void reorderProjectFolder(ReorderItemReqDto request) {
        DriveChannel driveChannel = getProjectDriveChannel(request.getDriveChannelSeq());
        commonDriveService.reorderFolder(driveChannel, request.getItemId(), request.getNewOrder());
    }

    // 프로젝트 드라이브 파일 다운로드
    public ResponseEntity<byte[]> downloadProjectFile(Long driveChannelSeq, Long documentSeq) {
        Document document = documentRepository.findByDocumentSeqAndDriveChannelDriveChannelSeq(documentSeq, driveChannelSeq)
            .orElseThrow(() -> new EntityNotFoundException("파일을 찾을 수 없습니다."));
        
        // 프로젝트 드라이브 채널인지 확인
        if (document.getDriveChannel().getWorkspaceType() != WorkSpaceType.PROJECT) {
            throw new IllegalArgumentException("프로젝트 드라이브 파일이 아닙니다: " + documentSeq);
        }

        try {
            byte[] fileContent = s3Uploader.download(document.getDocumentUrl());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", document.getDocumentName());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileContent);

        } catch (Exception e) {
            log.error("파일 다운로드 실패: {}", document.getDocumentName(), e);
            throw new IllegalStateException("파일 다운로드에 실패했습니다: " + document.getDocumentName(), e);
        }
    }

    // 프로젝트 드라이브 폴더 이름 변경
    public DriveItemDto renameProjectFolder(RenameFolderReqDto request) {
        DriveChannel driveChannel = getProjectDriveChannel(request.getDriveChannelSeq());
        return commonDriveService.renameFolder(driveChannel, request.getFolderSeq(), request.getNewFolderName());
    }

    // 프로젝트 드라이브 아이템 삭제
    public void deleteProjectItem(Long driveChannelSeq, Long userId, String itemType, Long itemId) {
        DriveChannel driveChannel = getProjectDriveChannel(driveChannelSeq);
        commonDriveService.deleteItem(driveChannel, userId, itemType, itemId);
    }

    // 프로젝트 드라이브 공유문서 상세 조회
    @Transactional(readOnly = true)
    public DocumentDetailDto getProjectDocument(Long driveChannelSeq, Long documentSeq) {
        Document document = documentRepository.findByDocumentSeqAndDriveChannelDriveChannelSeq(documentSeq, driveChannelSeq)
            .orElseThrow(() -> new EntityNotFoundException("문서를 찾을 수 없습니다."));
        
        // 프로젝트 드라이브 채널인지 확인
        if (document.getDriveChannel().getWorkspaceType() != WorkSpaceType.PROJECT) {
            throw new IllegalArgumentException("프로젝트 드라이브 문서가 아닙니다: " + documentSeq);
        }
        
        // 문서의 라인별 내용 조회
        List<DocumentLine> documentLines = documentLineRepository.findByDocumentDocumentSeqOrderByDocumentLineSeq(document.getDocumentSeq());
        
        return DocumentDetailDto.fromDocument(document, documentLines);
    }

//    // 프로젝트 드라이브 공유문서 내용 업데이트
//    //    // TODO: 추후 개발 예정
//    public DriveItemDto updateProjectDocumentContent(UpdateDocumentReqDto request) {
//        Document document = documentRepository.findByDocumentSeqAndDriveChannelDriveChannelSeq(request.getDocumentSeq(), request.getDriveChannelSeq())
//            .orElseThrow(() -> new EntityNotFoundException("문서를 찾을 수 없습니다."));
//
//        // 프로젝트 드라이브 채널인지 확인
//        if (document.getDriveChannel().getWorkspaceType() != WorkSpaceType.PROJECT) {
//            throw new IllegalArgumentException("프로젝트 드라이브 문서가 아닙니다: " + request.getDocumentSeq());
//        }
//
//        if (request.getContent() != null) {
//            updateDocumentContent(document, request.getContent());
//        }
//
//        return DriveItemDto.fromDocument(document);
//    }

    // 프로젝트 드라이브 공유문서 잠금/해제 토글
    public DriveItemDto toggleProjectDocumentLock(ToggleReqDto request) {
        Document document = documentRepository.findByDocumentSeqAndDriveChannelDriveChannelSeq(request.getDocumentSeq(), request.getDriveChannelSeq())
            .orElseThrow(() -> new EntityNotFoundException("문서를 찾을 수 없습니다."));
        
        // 프로젝트 드라이브 채널인지 확인
        if (document.getDriveChannel().getWorkspaceType() != WorkSpaceType.PROJECT) {
            throw new IllegalArgumentException("프로젝트 드라이브 문서가 아닙니다: " + request.getDocumentSeq());
        }
        
        String currentLockStatus = document.getYnLock();
        String newLockStatus = YnColumn.IS_TRUE.equals(currentLockStatus) ? YnColumn.IS_FALSE : YnColumn.IS_TRUE;
        document.updateLockStatus(newLockStatus);
        
        return DriveItemDto.fromDocument(document);
    }

    // 프로젝트 드라이브 공유문서 다운로드
    public ResponseEntity<byte[]> downloadProjectDocument(Long driveChannelSeq, Long documentSeq) {
        Document document = documentRepository.findByDocumentSeqAndDriveChannelDriveChannelSeq(documentSeq, driveChannelSeq)
            .orElseThrow(() -> new EntityNotFoundException("문서를 찾을 수 없습니다."));
        
        // 프로젝트 드라이브 채널인지 확인
        if (document.getDriveChannel().getWorkspaceType() != WorkSpaceType.PROJECT) {
            throw new IllegalArgumentException("프로젝트 드라이브 문서가 아닙니다: " + documentSeq);
        }
        
        try {
            String content = getDocumentContent(document);
            byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", document.getDocumentName() + ".txt");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(contentBytes);
                
        } catch (Exception e) {
            log.error("문서 다운로드 실패: {}", document.getDocumentName(), e);
            throw new MultipartException("문서 다운로드에 실패했습니다: " + document.getDocumentName(), e);
        }
    }

    // 문서 내용 업데이트 (내부 메서드)
    private void updateDocumentContent(Document document, String content) {
        try {
            List<DocumentLine> existingLines = documentLineRepository.findByDocumentDocumentSeqOrderByDocumentLineSeq(document.getDocumentSeq());
            documentLineRepository.deleteAll(existingLines);
            
            // 새 내용을 라인별로 저장
            String[] lines = content.split("\n");
            List<DocumentLine> newLines = new ArrayList<>();
            
            for (int i = 0; i < lines.length; i++) {
                DocumentLine line = DocumentLine.builder()
                    .documentContent(lines[i])
                    .documentLineSeq((long) (i + 1))
                    .document(document)
                    .build();
                newLines.add(line);
            }
            
            documentLineRepository.saveAll(newLines);
            
        } catch (Exception e) {
            log.error("문서 내용 업데이트 실패", e);
            throw new IllegalStateException("문서 내용 업데이트에 실패했습니다.", e);
        }
    }

    // 문서 내용 조회 (내부 메서드)
    private String getDocumentContent(Document document) {
        try {
            List<DocumentLine> documentLines = documentLineRepository.findByDocumentDocumentSeqOrderByDocumentLineSeq(document.getDocumentSeq());

            if (documentLines.isEmpty()) {
                return "문서 내용이 없습니다.";
            }

            return documentLines.stream()
                .map(DocumentLine::getDocumentContent)
                .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            log.error("문서 내용 조회 실패", e);
            return "문서 내용을 불러올 수 없습니다.";
        }
    }


    // TODO: 프로젝트 스페이스 생성자가 진행할 예정.
    // 프로젝트 드라이브 채널 생성
//    public DriveItemDto createProjectDriveChannel(Long userId, CreateDriveChannelRequest request) {
//        DriveChannel channel = DriveChannel.builder()
//                .driveChannelName(request.getDriveChannelName())
//                .workspaceSeq(request.getWorkspaceSeq())
//                .workspaceType(WorkSpaceType.PROJECT)
//                .build();
//
//        return null;
//    }
}
