package com.team404.synco.drive.service;

import com.team404.synco.common.constant.DocumentType;
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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PersonalDriveService {

    private final CommonDriveService commonDriveService;
    private final DocumentRepository documentRepository;
    private final DocumentLineRepository documentLineRepository;
    private final DriveChannelRepository driveChannelRepository;
    private final S3Uploader s3Uploader;

    // 드라이브 생성
    public Long createChannel(DriveCreateReqDto driveCreateReqDto) {
        return driveChannelRepository.save(driveCreateReqDto.toEntity()).getDriveChannelSeq();
    }

    // 개인 드라이브 아이템 목록 조회
    @Transactional(readOnly = true)
    public Page<DriveItemDto> getPersonalDriveItems(Long driveChannelSeq, Long parentFolderId, Pageable pageable, String sortBy, String sortOrder) {
        DriveChannel personalDrive = getPersonalDriveChannel(driveChannelSeq);
        return commonDriveService.getDriveItems(personalDrive, parentFolderId, pageable, sortBy, sortOrder);
    }

    // 개인 드라이브 폴더 생성
    public DriveItemDto createPersonalFolder(CreateFolderReqDto createFolderReqDto) {
        DriveChannel personalDrive = getPersonalDriveChannel(createFolderReqDto.getDriveChannelSeq());
        return commonDriveService.createFolder(personalDrive, createFolderReqDto.getFolderName(), createFolderReqDto.getParentFolderSeq());
    }

    // 개인 드라이브 공유문서 생성
    public DriveItemDto createPersonalSharedDoc(Long userId, CreateSharedDocReqDto createSharedDocReqDto) {
        DriveChannel personalDriveChannel = getPersonalDriveChannel(createSharedDocReqDto.getDriveChannelSeq());
        return commonDriveService.createSharedDoc(personalDriveChannel, userId, createSharedDocReqDto.getDocumentName(),
                createSharedDocReqDto.getParentFolderSeq(), createSharedDocReqDto.getIsLocked());
    }

    // 개인 드라이브 파일 업로드
    public List<DriveItemDto> uploadPersonalFiles(Long userId, FileUploadReqDto fileUploadReqDto) {
        DriveChannel personalDrive = getPersonalDriveChannel(fileUploadReqDto.getDriveChannelSeq());
        return commonDriveService.uploadFiles(personalDrive, userId, fileUploadReqDto.getFiles(), fileUploadReqDto.getParentFolderSeq());
    }

    // 개인 드라이브 아이템 이동
    public void movePersonalItem(MoveItemReqDto moveItemReqDto) {
        DriveChannel personalDrive = getPersonalDriveChannel(moveItemReqDto.getDriveChannelSeq());
        commonDriveService.moveItem(personalDrive, moveItemReqDto.getItemType(), moveItemReqDto.getItemId(), moveItemReqDto.getNewParentSeq());
    }

    // 개인 드라이브 폴더 순서 변경
    public void reorderPersonalFolder(ReorderItemReqDto reorderItemReqDto) {
        DriveChannel personalDrive = getPersonalDriveChannel(reorderItemReqDto.getDriveChannelSeq());
        commonDriveService.reorderFolder(personalDrive, reorderItemReqDto.getItemId(), reorderItemReqDto.getNewOrder());
    }

    // 개인 드라이브 파일 다운로드
    public ResponseEntity<byte[]> downloadPersonalFile(Long driveChannelSeq, Long documentSeq) {
        Document document = documentRepository.findById(documentSeq).orElseThrow(() -> new EntityNotFoundException("파일을 찾을 수 없습니다."));

        // 개인 드라이브 채널인지 확인
        if (document.getDriveChannel().getWorkSpaceType() != WorkSpaceType.INDIVIDUAL) {
            throw new IllegalArgumentException("개인 드라이브 파일이 아닙니다: " + documentSeq);
        }

        // 요청한 채널과 문서의 채널이 일치하는지 확인
        if (!document.getDriveChannel().getDriveChannelSeq().equals(driveChannelSeq)) {
            throw new IllegalArgumentException("요청한 채널의 파일이 아닙니다: " + documentSeq);
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
            log.error("파일 다운로드 실패: {}", document.getDocumentName(), e);
            throw new IllegalStateException("파일 다운로드에 실패했습니다: " + document.getDocumentName(), e);
        }
    }

    // 개인 드라이브 폴더 이름 변경
    public DriveItemDto renamePersonalFolder(RenameFolderReqDto renameFolderReqDto) {
        DriveChannel personalDrive = getPersonalDriveChannel(renameFolderReqDto.getDriveChannelSeq());
        return commonDriveService.renameFolder(personalDrive, renameFolderReqDto.getFolderSeq(), renameFolderReqDto.getNewFolderName());
    }

    // 개인 드라이브 아이템 삭제
    public void deletePersonalItem(Long driveChannelSeq, Long userId, String itemType, Long itemId) {
        DriveChannel personalDrive = getPersonalDriveChannel(driveChannelSeq);
        commonDriveService.deleteItem(personalDrive, userId, itemType, itemId);
    }

//    // 개인 드라이브 공유문서 상세 조회
//    @Transactional(readOnly = true)
//    public DocumentDetailDto getPersonalDocument(Long driveChannelSeq, Long documentSeq) {
//        Document document = documentRepository.findByDocumentSeqAndDriveChannelDriveChannelSeq(documentSeq, driveChannelSeq)
//                .orElseThrow(() -> new EntityNotFoundException("문서를 찾을 수 없습니다."));
//
//        // 개인 드라이브 채널인지 확인
//        if (document.getDriveChannel().getWorkSpaceType() != WorkSpaceType.INDIVIDUAL) {
//            throw new IllegalArgumentException("개인 드라이브 문서가 아닙니다: " + documentSeq);
//        }
//
//        // DB에서 조회
//        List<DocumentLine> documentLines = documentLineRepository.findByDocumentDocumentSeqOrderByDocumentLineSeq(document.getDocumentSeq());
//
//        // 응답 (계층 구조 고려한 정렬)
//        return DocumentDetailDto.fromEntity(document, documentLines);
//    }

    // 개인 드라이브 공유문서 잠금/해제 토글
    public DriveItemDto togglePersonalDocumentLock(ToggleReqDto toggleReqDto) {
        Document document = documentRepository.findByDocumentSeqAndDriveChannelDriveChannelSeq(toggleReqDto.getDocumentSeq(), toggleReqDto.getDriveChannelSeq())
                .orElseThrow(() -> new EntityNotFoundException("문서를 찾을 수 없습니다."));

        // 개인 드라이브 채널인지 확인
        if (document.getDriveChannel().getWorkSpaceType() != WorkSpaceType.INDIVIDUAL) {
            throw new IllegalArgumentException("개인 드라이브 문서가 아닙니다: " + toggleReqDto.getDocumentSeq());
        }

        String currentLockStatus = document.getYnLock();
        String newLockStatus = YnColumn.IS_TRUE.equals(currentLockStatus) ? YnColumn.IS_FALSE : YnColumn.IS_TRUE;
        document.updateLockStatus(newLockStatus);

        return DriveItemDto.fromDocument(document);
    }

    // 개인 드라이브 공유문서 다운로드
    public ResponseEntity<byte[]> downloadPersonalDocument(Long driveChannelSeq, Long documentSeq) {
        log.info("개인 공유문서 다운로드 시작 - DriveChannelSeq: {}, DocumentSeq: {}", driveChannelSeq, documentSeq);
        
        Document document = documentRepository.findByDocumentSeqAndDriveChannelDriveChannelSeq(documentSeq, driveChannelSeq)
                .orElseThrow(() -> new EntityNotFoundException("문서를 찾을 수 없습니다: " + documentSeq));

        // 개인 드라이브 채널인지 확인
        if (document.getDriveChannel().getWorkSpaceType() != WorkSpaceType.INDIVIDUAL) {
            throw new IllegalArgumentException("개인 드라이브 문서가 아닙니다: " + documentSeq);
        }
        
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
            
            log.info("개인 공유문서 다운로드 완료 - DocumentSeq: {}, 파일명: {}", documentSeq, fileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(contentBytes);
                    
        } catch (Exception e) {
            log.error("개인 공유문서 다운로드 실패 - DocumentSeq: {}", documentSeq, e);
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

    // 개인 드라이브 채널 조회
    private DriveChannel getPersonalDriveChannel(Long driveChannelSeq) {
        DriveChannel channel = driveChannelRepository.findById(driveChannelSeq).orElseThrow(() -> new EntityNotFoundException("드라이브 채널을 찾을 수 없습니다: " + driveChannelSeq));

        // 개인 드라이브 채널인지 확인
        if (channel.getWorkSpaceType() != WorkSpaceType.INDIVIDUAL) {
            throw new IllegalArgumentException("개인 드라이브 채널이 아닙니다: " + driveChannelSeq);
        }

        return channel;
    }

//    // 문서 내용 조회
//    private String getDocumentContent(Document document) {
//        try {
//            List<DocumentLine> documentLines = documentLineRepository.findByDocumentDocumentSeqOrderByDocumentLineSeq(document.getDocumentSeq());
//
//            if (documentLines.isEmpty()) {
//                return "문서 내용이 없습니다.";
//            }
//
//            return documentLines.stream()
//                    .map(DocumentLine::getDocumentContent)
//                    .collect(Collectors.joining("\n"));
//        } catch (Exception e) {
//            return "문서 내용을 불러올 수 없습니다.";
//        }
//    }

    // 개인 드라이브 폴더 트리 조회 
    @Transactional(readOnly = true)
    public List<FolderTreeDto> getPersonalFolderTree(Long driveChannelSeq) {
        DriveChannel driveChannel = getPersonalDriveChannel(driveChannelSeq);
        return commonDriveService.getFolderTree(driveChannel.getDriveChannelSeq());
    }

    // 개인 드라이브 문서 이름 변경
    public void renamePersonalDocument(RenameDocumentReqDto renameDocumentReqDto) {
        DriveChannel driveChannel = getPersonalDriveChannel(renameDocumentReqDto.getDriveChannelSeq());
        commonDriveService.renameDocument(driveChannel, renameDocumentReqDto.getDocumentSeq(), renameDocumentReqDto.getNewDocumentName());
    }
}
