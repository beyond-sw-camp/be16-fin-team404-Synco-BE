package com.team404.synco.drive.service;

import com.team404.synco.common.constant.WorkSpaceType;
import com.team404.synco.common.constant.YnColumn;
import com.team404.synco.common.service.S3Uploader;
import com.team404.synco.drive.dto.*;
import com.team404.synco.drive.entity.Document;
import com.team404.synco.drive.entity.DocumentLine;
import com.team404.synco.drive.entity.DriveChannel;
import com.team404.synco.drive.repository.DocumentLineRepository;
import com.team404.synco.drive.repository.DocumentRepository;
import com.team404.synco.drive.repository.DriveChannelRepository;
import com.team404.synco.drive.util.ContentTypeUtil;
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

import java.net.URLEncoder;
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

    // 드라이브 생성
    public Long createChannel(DriveCreateReqDto driveCreateReqDto){
        return driveChannelRepository.save(driveCreateReqDto.toEntity()).getDriveChannelSeq();
    }

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
    public DriveItemDto createProjectFolder(CreateFolderReqDto createFolderReqDto) {
        DriveChannel driveChannel = getProjectDriveChannel(createFolderReqDto.getDriveChannelSeq());
        return commonDriveService.createFolder(driveChannel, createFolderReqDto.getFolderName(), createFolderReqDto.getParentFolderSeq());
    }

    // 프로젝트 드라이브 공유문서 생성
    public DriveItemDto createProjectSharedDoc(Long userId, CreateSharedDocReqDto createSharedDocReqDto) {
        DriveChannel driveChannel = getProjectDriveChannel(createSharedDocReqDto.getDriveChannelSeq());
        return commonDriveService.createSharedDoc(driveChannel,userId, createSharedDocReqDto.getDocumentName(), createSharedDocReqDto.getParentFolderSeq(), createSharedDocReqDto.getIsLocked());
    }

    // 프로젝트 드라이브 파일 업로드
    public List<DriveItemDto> uploadProjectFiles(Long userId, FileUploadReqDto fileUploadReqDto) {
        DriveChannel driveChannel = getProjectDriveChannel(fileUploadReqDto.getDriveChannelSeq());
        return commonDriveService.uploadFiles(driveChannel, userId, fileUploadReqDto.getFiles(), fileUploadReqDto.getParentFolderSeq());
    }

    // 프로젝트 드라이브 아이템 이동
    public void moveProjectItem(MoveItemReqDto moveItemReqDto) {
        DriveChannel driveChannel = getProjectDriveChannel(moveItemReqDto.getDriveChannelSeq());
        commonDriveService.moveItem(driveChannel, moveItemReqDto.getItemType(), moveItemReqDto.getItemId(), moveItemReqDto.getNewParentSeq());
    }

    // 프로젝트 드라이브 폴더 순서 변경
    public void reorderProjectFolder(ReorderItemReqDto reorderItemReqDto) {
        DriveChannel driveChannel = getProjectDriveChannel(reorderItemReqDto.getDriveChannelSeq());
        commonDriveService.reorderFolder(driveChannel, reorderItemReqDto.getItemId(), reorderItemReqDto.getNewOrder());
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
            
            // 파일 확장자에 따른 Content-Type 설정
            String contentType = ContentTypeUtil.getContentType(document.getDocumentName());
            headers.setContentType(MediaType.parseMediaType(contentType));
            
            // 파일명 인코딩 처리
            String encodedFileName = URLEncoder.encode(document.getDocumentName(), StandardCharsets.UTF_8);
            headers.setContentDispositionFormData("attachment", encodedFileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileContent);

        } catch (Exception e) {
            throw new IllegalStateException("파일 다운로드에 실패했습니다: " + document.getDocumentName(), e);
        }
    }

    // 프로젝트 드라이브 폴더 이름 변경
    public DriveItemDto renameProjectFolder(RenameFolderReqDto renameFolderReqDto) {
        DriveChannel driveChannel = getProjectDriveChannel(renameFolderReqDto.getDriveChannelSeq());
        return commonDriveService.renameFolder(driveChannel, renameFolderReqDto.getFolderSeq(), renameFolderReqDto.getNewFolderName());
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
    public DriveItemDto toggleProjectDocumentLock(ToggleReqDto toggleReqDto) {
        Document document = documentRepository.findByDocumentSeqAndDriveChannelDriveChannelSeq(toggleReqDto.getDocumentSeq(), toggleReqDto.getDriveChannelSeq())
            .orElseThrow(() -> new EntityNotFoundException("문서를 찾을 수 없습니다."));
        
        // 프로젝트 드라이브 채널인지 확인
        if (document.getDriveChannel().getWorkspaceType() != WorkSpaceType.PROJECT) {
            throw new IllegalArgumentException("프로젝트 드라이브 문서가 아닙니다: " + toggleReqDto.getDocumentSeq());
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
            throw new MultipartException("문서 다운로드에 실패했습니다: " + document.getDocumentName(), e);
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
            return "문서 내용을 불러올 수 없습니다.";
        }
    }
}
