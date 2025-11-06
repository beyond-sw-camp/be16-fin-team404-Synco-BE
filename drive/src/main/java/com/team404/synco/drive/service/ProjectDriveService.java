package com.team404.synco.drive.service;

import com.team404.synco.common.constant.DocumentType;
import com.team404.synco.common.constant.WorkSpaceType;
import com.team404.synco.common.constant.YnColumn;
import com.team404.synco.common.service.RedisEventPublisher;
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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProjectDriveService {

    private final CommonDriveService commonDriveService;
    private final RedisEventPublisher redisEventPublisher;
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
        getProjectDriveChannel(driveChannelSeq);
        return commonDriveService.getFolderTree(driveChannelSeq);
    }

    // 프로젝트 드라이브 공유문서 다운로드
    public ResponseEntity<byte[]> downloadProjectDocument(Long driveChannelSeq, Long documentSeq) {
        log.info("프로젝트 공유문서 다운로드 시작 - DriveChannelSeq: {}, DocumentSeq: {}", driveChannelSeq, documentSeq);
        
        // 프로젝트 드라이브 채널 조회 및 검증
        getProjectDriveChannel(driveChannelSeq);
        
        // 문서가 해당 드라이브 채널에 속하는지 확인
        Document document = documentRepository.findByDocumentSeqAndDriveChannelDriveChannelSeq(documentSeq, driveChannelSeq)
                .orElseThrow(() -> new EntityNotFoundException("문서를 찾을 수 없습니다: " + documentSeq));
        
        // 공유문서인지 확인
        if (document.getDocumentType() != DocumentType.CUSTOM) {
            throw new IllegalArgumentException("공유문서가 아닙니다: " + documentSeq);
        }
        
        try {
            // 문서 내용을 txt 형태로 변환
            String documentContent = getDocumentContentAsText(document);
            byte[] contentBytes = documentContent.getBytes(StandardCharsets.UTF_8);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            
            // 파일명 인코딩 처리 (.txt 확장자 추가)
            String fileName = document.getDocumentName() + ".txt";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
            headers.setContentDispositionFormData("attachment", encodedFileName);
            
            log.info("프로젝트 공유문서 다운로드 완료 - DocumentSeq: {}, 파일명: {}", documentSeq, fileName);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(contentBytes);
                    
        } catch (Exception e) {
            log.error("공유문서 다운로드 실패 - DocumentSeq: {}", documentSeq, e);
            throw new IllegalStateException("공유문서 다운로드에 실패했습니다: " + document.getDocumentName(), e);
        }
    }
    
    /**
     * 공유문서의 모든 라인을 순서대로 텍스트로 변환
     */
    private String getDocumentContentAsText(Document document) {
        // 문서의 모든 DocumentLine들을 조회
        List<DocumentLine> documentLines = documentLineRepository.findByDocumentDocumentSeqOrderByDocumentLineSeq(document.getDocumentSeq());
        
        if (documentLines.isEmpty()) {
            return ""; // 빈 문서
        }
        
        // 연결 리스트 순서대로 정렬
        List<DocumentLine> orderedLines = buildOrderedDocumentLines(documentLines);
        
        // 텍스트로 변환 (HTML 태그 제거)
        StringBuilder content = new StringBuilder();
        for (DocumentLine line : orderedLines) {
            if (line.getDocumentContent() != null) {
                String cleanText = removeHtmlTags(line.getDocumentContent());
                content.append(cleanText).append("\n");
            }
        }
        
        return content.toString();
    }
    
    /**
     * DocumentLine 리스트를 연결 리스트 순서대로 정렬
     */
    private List<DocumentLine> buildOrderedDocumentLines(List<DocumentLine> documentLines) {
        if (documentLines.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Map으로 빠른 조회를 위한 인덱스 생성
        Map<String, DocumentLine> lineMap = new HashMap<>();
        Map<String, String> nextLineMap = new HashMap<>(); // prevId -> lineId 매핑
        
        for (DocumentLine line : documentLines) {
            lineMap.put(line.getLineId(), line);
            
            // 다음 라인 매핑 생성 (prevId -> lineId)
            if (line.getPrevId() != null && !line.getPrevId().isEmpty()) {
                nextLineMap.put(line.getPrevId(), line.getLineId());
            }
        }
        
        List<DocumentLine> orderedList = new ArrayList<>(documentLines.size());
        
        // 첫 번째 라인 찾기 (prevId가 null인 라인)
        String firstLineId = null;
        for (DocumentLine line : documentLines) {
            if (line.getPrevId() == null || line.getPrevId().isEmpty()) {
                firstLineId = line.getLineId();
                break;
            }
        }
        
        // 연결 리스트 순서대로 순회
        String currentLineId = firstLineId;
        while (currentLineId != null) {
            DocumentLine currentLine = lineMap.get(currentLineId);
            if (currentLine != null) {
                orderedList.add(currentLine);
                currentLineId = nextLineMap.get(currentLineId);
            } else {
                break;
            }
        }
        
        return orderedList;
    }
    
    /**
     * HTML 태그를 제거하고 순수 텍스트만 추출
     */
    private String removeHtmlTags(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return "";
        }
        
        // HTML 태그 제거 (정규식 사용)
        String cleanText = htmlContent.replaceAll("<[^>]*>", "");
        
        // HTML 엔티티 디코딩
        cleanText = cleanText.replace("&lt;", "<")
                           .replace("&gt;", ">")
                           .replace("&amp;", "&")
                           .replace("&quot;", "\"")
                           .replace("&#39;", "'")
                           .replace("&nbsp;", " ");
        
        // 연속된 공백을 하나로 변환
        cleanText = cleanText.replaceAll("\\s+", " ");
        
        // 앞뒤 공백 제거
        cleanText = cleanText.trim();
        
        return cleanText;
    }

    // 프로젝트 드라이브 문서 이름 변경
    public void renameProjectDocument(RenameDocumentReqDto renameDocumentReqDto) {
        DriveChannel driveChannel = getProjectDriveChannel(renameDocumentReqDto.getDriveChannelSeq());
        commonDriveService.renameDocument(driveChannel, renameDocumentReqDto.getDocumentSeq(), renameDocumentReqDto.getNewDocumentName());
    }

    // 드라이브 삭제(WorkSpace 삭제시)
    public void deleteDrive(Long workSpaceSeq){
        driveChannelRepository.deleteByWorkspaceSeq(workSpaceSeq);
    }
}
