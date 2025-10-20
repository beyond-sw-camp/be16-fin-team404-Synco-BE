package com.team404.synco.drive.service;

import com.team404.synco.common.constant.WorkSpaceType;
import com.team404.synco.common.constant.YnColumn;
import com.team404.synco.common.service.S3Uploader;
import com.team404.synco.drive.dto.*;
import com.team404.synco.drive.entity.Document;
import com.team404.synco.drive.entity.DocumentLine;
import com.team404.synco.drive.entity.DocumentMessageMethod;
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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProjectDriveService {

    private final CommonDriveService commonDriveService;
    private final DocumentRepository documentRepository;
    private final DriveChannelRepository driveChannelRepository;
    private final DocumentLineRepository documentLineRepository;
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
        if (channel.getWorkSpaceType() != WorkSpaceType.PROJECT) {
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
        if (document.getDriveChannel().getWorkSpaceType() != WorkSpaceType.PROJECT) {
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

    // 프로젝트 드라이브 공유문서 목록 조회
    @Transactional(readOnly = true)
    public List<DocDetailListResDto> getProjectSharedDocuments(Long driveChannelSeq, Long document) {
        log.info("프로젝트 공유문서 목록 조회 시작 - DriveChannelSeq: {}, DocumentSeq: {}", driveChannelSeq, document);
        
        // 프로젝트 드라이브 채널 조회 및 검증
        DriveChannel driveChannel = getProjectDriveChannel(driveChannelSeq);
        
        // 문서가 해당 드라이브 채널에 속하는지 확인
        Document targetDocument = documentRepository.findById(document)
                .orElseThrow(() -> new EntityNotFoundException("문서를 찾을 수 없습니다: " + document));
        
        if (!targetDocument.getDriveChannel().getDriveChannelSeq().equals(driveChannelSeq)) {
            throw new IllegalArgumentException("문서가 해당 드라이브 채널에 속하지 않습니다.");
        }
        
        // 공유문서 목록 조회
        List<DocDetailListResDto> result = commonDriveService.getSharedDoc(driveChannel, document);
        
        log.info("프로젝트 공유문서 목록 조회 완료 - DriveChannelSeq: {}, DocumentSeq: {}, 라인 수: {}", 
                driveChannelSeq, document, result.size());
        return result;
    }

    public void updateDocumentLine(EditorMessageDto message){
        DocumentLine documentLine = documentLineRepository.findByLineId(message.getLineId())
                .orElseThrow(()->new EntityNotFoundException("해당 라인이 존재하지 않습니다." + message.getLineId()));
        documentLine.updateContent(message.getContent());
    }

    public void createDocumentLine(EditorMessageDto message){
        Document document = documentRepository.findById(Long.valueOf(message.getDocumentId()))
                .orElseThrow(()->new EntityNotFoundException("해당 문서가 존재하지 않습니다."));

        // 만약 중간에 끼어들어갈 경우 순서 바꿔주기
        Optional<DocumentLine> documentLine = documentLineRepository.findByPrevId(message.getPrevLineId());
        documentLine.ifPresent(line -> line.updatePrevId(message.getLineId()));

        // 1. DTO를 Entity로 변환합니다.
        DocumentLine newDocumentLine = null;
        newDocumentLine = DocumentLine.builder()
                .prevId(message.getPrevLineId())
                .document(document)
                .lineId(message.getLineId())
                .documentContent(message.getContent())
                .build();

        // 2. Repository를 통해 데이터베이스에 저장합니다.
        documentLineRepository.save(newDocumentLine);
    }

    public void deleteDocumentLine(EditorMessageDto message){
        // 만약 뒷 라인이 있다면 앞단과 연결 시켜주기
        Optional<DocumentLine> documentLine = documentLineRepository.findByPrevId(message.getLineId());
        System.out.println(message.getPrevLineId());
        documentLine.ifPresent(line -> line.updatePrevId(message.getPrevLineId()));
        // 현재 라인 삭제
        documentLineRepository.delete(documentLineRepository.findByLineId(message.getLineId()).orElseThrow(()->new EntityNotFoundException("해당 라인이 존재하지 않습니다.")));
    }

    // 배치 생성
    public void createDocumentLines(EditorMessageDto message) {
        if (message.getChanges() == null || message.getChanges().isEmpty()) {
            return;
        }

        Document document = documentRepository.findById(Long.valueOf(message.getDocumentId()))
                .orElseThrow(() -> new EntityNotFoundException("해당 문서가 존재하지 않습니다."));

        for (EditorMessageDto.LineChange change : message.getChanges()) {
            // 만약 중간에 끼어들어갈 경우 순서 바꿔주기
            Optional<DocumentLine> documentLine = documentLineRepository.findByPrevId(change.getPrevLineId());
            documentLine.ifPresent(line -> line.updatePrevId(change.getLineId()));

            DocumentLine newDocumentLine = DocumentLine.builder()
                    .prevId(change.getPrevLineId())
                    .document(document)
                    .lineId(change.getLineId())
                    .documentContent(change.getContent())
                    .build();

            documentLineRepository.save(newDocumentLine);
        }
    }

    // 배치 수정
    public void updateDocumentLines(EditorMessageDto message) {
        if (message.getChanges() == null || message.getChanges().isEmpty()) {
            return;
        }

        for (EditorMessageDto.LineChange change : message.getChanges()) {
            DocumentLine documentLine = documentLineRepository.findByLineId(change.getLineId())
                    .orElseThrow(() -> new EntityNotFoundException("해당 라인이 존재하지 않습니다." + change.getLineId()));
            documentLine.updateContent(change.getContent());
        }
    }

    // 배치 삭제
    public void deleteDocumentLines(EditorMessageDto message) {
        if (message.getChanges() == null || message.getChanges().isEmpty()) {
            return;
        }

        for (EditorMessageDto.LineChange change : message.getChanges()) {
            // 만약 뒷 라인이 있다면 앞단과 연결 시켜주기
            Optional<DocumentLine> documentLine = documentLineRepository.findByPrevId(change.getLineId());
            documentLine.ifPresent(line -> line.updatePrevId(change.getPrevLineId()));
            
            // 현재 라인 삭제
            documentLineRepository.delete(
                documentLineRepository.findByLineId(change.getLineId())
                    .orElseThrow(() -> new EntityNotFoundException("해당 라인이 존재하지 않습니다."))
            );
        }
    }



    // 프로젝트 드라이브 공유문서 잠금/해제 토글
    public DriveItemDto toggleProjectDocumentLock(ToggleReqDto toggleReqDto) {
        Document document = documentRepository.findByDocumentSeqAndDriveChannelDriveChannelSeq(toggleReqDto.getDocumentSeq(), toggleReqDto.getDriveChannelSeq())
            .orElseThrow(() -> new EntityNotFoundException("문서를 찾을 수 없습니다."));

        // 프로젝트 드라이브 채널인지 확인
        if (document.getDriveChannel().getWorkSpaceType() != WorkSpaceType.PROJECT) {
            throw new IllegalArgumentException("프로젝트 드라이브 문서가 아닙니다: " + toggleReqDto.getDocumentSeq());
        }

        String currentLockStatus = document.getYnLock();
        String newLockStatus = YnColumn.IS_TRUE.equals(currentLockStatus) ? YnColumn.IS_FALSE : YnColumn.IS_TRUE;
        document.updateLockStatus(newLockStatus);

        return DriveItemDto.fromDocument(document);
    }

    // 프로젝트 드라이브 폴더 트리 조회
    @Transactional(readOnly = true)
    public List<FolderTreeDto> getProjectFolderTree(Long driveChannelSeq) {
        DriveChannel driveChannel = getProjectDriveChannel(driveChannelSeq);
        return commonDriveService.getFolderTree(driveChannelSeq);
    }

    // 프로젝트 드라이브 문서 이름 변경
    public void renameProjectDocument(RenameDocumentReqDto renameDocumentReqDto) {
        DriveChannel driveChannel = getProjectDriveChannel(renameDocumentReqDto.getDriveChannelSeq());
        commonDriveService.renameDocument(driveChannel, renameDocumentReqDto.getDocumentSeq(), renameDocumentReqDto.getNewDocumentName());
    }
}
