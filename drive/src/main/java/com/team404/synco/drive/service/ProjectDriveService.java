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

    // 프로젝트 드라이브 공유문서 라인 생성
    public void createBlock(UpdateDocumentReqDto updateDocumentReqDto) {

        Document document = documentRepository.findById(updateDocumentReqDto.getDocumentId()).orElseThrow(() -> new EntityNotFoundException("문서를 찾을 수 없습니다: " + updateDocumentReqDto.getDocumentId()));

        DocumentLine checkLine = documentLineRepository.findByFeId(updateDocumentReqDto.getDocumentFeId()).orElse(null);
        if (checkLine != null) {
            throw new IllegalArgumentException("이미 존재하는 FE ID입니다: " + updateDocumentReqDto.getDocumentFeId());
        }

        // 부모 라인 조회 (prevFeId가 부모 라인의 feId)
        DocumentLine parentLine = null;
        if (updateDocumentReqDto.getParentDocumentLineSeq() != null && !updateDocumentReqDto.getParentDocumentLineSeq().isEmpty()) {
            parentLine = documentLineRepository.findByFeId(updateDocumentReqDto.getParentDocumentLineSeq())
                    .orElseThrow(() -> new EntityNotFoundException("부모 라인을 찾을 수 없습니다: " + updateDocumentReqDto.getParentDocumentLineSeq()));
        }

        // 새 블록 생성
        DocumentLine newLine = DocumentLine.builder()
                .document(document)
                .feId(updateDocumentReqDto.getDocumentFeId())
                .documentContent(updateDocumentReqDto.getContent())
                .prevFeId(updateDocumentReqDto.getParentDocumentLineSeq()) // 부모 라인의 feId를 prevFeId에 저장
                .type(updateDocumentReqDto.getBlockType())
                .level(updateDocumentReqDto.getBlockLevel())
                .indent(updateDocumentReqDto.getBlockIndent())
                .build();

        documentLineRepository.save(newLine);

        // 🔥 중요: 중간 삽입 시 기존 블록들의 연결 재구성
        if (parentLine != null) {
            // 부모 라인의 다음 블록이 새 블록을 참조하도록 변경
            documentLineRepository.findByPrevFeId(parentLine.getFeId())
                    .ifPresent(nextBlock -> nextBlock.updatePrevFeId(updateDocumentReqDto.getDocumentFeId()));
        }
    }

    // 프로젝트 드라이브 공유문서 라인 수정
    public void updateBlock(UpdateDocumentReqDto updateDocumentReqDto) {
        // 수정할 블록 조회
        DocumentLine blockToUpdate = documentLineRepository.findByFeId(updateDocumentReqDto.getDocumentFeId())
                .orElseThrow(() -> new EntityNotFoundException("수정할 블록을 찾을 수 없습니다: " + updateDocumentReqDto.getDocumentFeId()));

        blockToUpdate.updateAllInfo(
                updateDocumentReqDto.getContent(),
                updateDocumentReqDto.getBlockType(),
                updateDocumentReqDto.getBlockLevel(),
                updateDocumentReqDto.getBlockIndent()
        );
    }

    // 프로젝트 드라이브 공유문서 라인 세부사항 수정
    public void patchBlockDetails(UpdateDocumentReqDto updateDocumentReqDto) {
        // 수정할 블록 조회
        DocumentLine blockToUpdate = documentLineRepository.findByFeId(updateDocumentReqDto.getDocumentFeId())
                .orElseThrow(() -> new EntityNotFoundException("수정할 블록을 찾을 수 없습니다: " + updateDocumentReqDto.getDocumentFeId()));

        // 세부사항에 따라 업데이트 수행
        if(updateDocumentReqDto.getMethod().equals(DocumentMessageMethod.UPDATE_INDENT_BLOCK)){
            blockToUpdate.updateIndent(updateDocumentReqDto.getBlockIndent());
        } else if(updateDocumentReqDto.getMethod().equals(DocumentMessageMethod.HOT_UPDATE_CONTENTS_BLOCK)){
            blockToUpdate.updateDocumentContent(updateDocumentReqDto.getContent());
        }
    }

    // 프로젝트 드라이브 공유문서 라인 순서 변경 (성능 최적화)
    public void changeOrderBlock(UpdateDocumentReqDto updateDocumentReqDto) {
        String movingFeId = updateDocumentReqDto.getDocumentFeId();
        String newPrevFeId = updateDocumentReqDto.getParentDocumentLineSeq(); // 새로운 이전 블록의 feId

        // 1. 이동할 블록 조회
        DocumentLine movingBlock = documentLineRepository.findByFeId(movingFeId)
                .orElseThrow(() -> new EntityNotFoundException("이동할 블록을 찾을 수 없습니다: " + movingFeId));

        // 2. 기존 위치에서 제거 (이동할 블록의 다음 블록이 이동할 블록의 이전 블록을 참조하도록)
        DocumentLine nextBlock = documentLineRepository.findByPrevFeId(movingFeId).orElse(null);
        if (nextBlock != null) {
            nextBlock.updatePrevFeId(movingBlock.getPrevFeId());
        }

        // 3. 새로운 위치에 삽입
        if (newPrevFeId != null && !newPrevFeId.isEmpty()) {
            // 새로운 이전 블록의 다음 블록이 이동할 블록을 참조하도록
            DocumentLine newNextBlock = documentLineRepository.findByPrevFeId(newPrevFeId).orElse(null);
            if (newNextBlock != null) {
                newNextBlock.updatePrevFeId(movingFeId);
            }
        }

        // 4. 이동할 블록의 prevFeId 업데이트
        movingBlock.updatePrevFeId(newPrevFeId);
    }

    // 프로젝트 드라이브 공유문서 라인 삭제 (성능 최적화)
    public void deleteBlock(UpdateDocumentReqDto updateDocumentReqDto) {
        String blockFeId = updateDocumentReqDto.getDocumentFeId();

        // 1. 삭제할 블록 조회
        DocumentLine blockToDelete = documentLineRepository.findByFeId(blockFeId)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 블록을 찾을 수 없습니다: " + blockFeId));

        // 2. 연결 리스트 재구성 (성능 최적화: 한 번의 쿼리로 처리)
        DocumentLine nextBlock = documentLineRepository.findByPrevFeId(blockFeId).orElse(null);
        if (nextBlock != null) {
            // 다음 블록이 존재하는 경우에만 연결 재구성
            nextBlock.updatePrevFeId(blockToDelete.getPrevFeId());
        }
        // 다음 블록이 없으면 마지막 블록이므로 아무것도 안 함

        // 3. 블록 삭제
        documentLineRepository.delete(blockToDelete);
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
