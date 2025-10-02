package com.team404.synco.drive.service;

import com.team404.synco.common.constant.DocumentType;
import com.team404.synco.common.constant.YnColumn;
import com.team404.synco.common.service.S3Uploader;
import com.team404.synco.drive.dto.DriveItemDto;
import com.team404.synco.drive.entity.Document;
import com.team404.synco.drive.entity.DriveChannel;
import com.team404.synco.drive.entity.Folder;
import com.team404.synco.drive.repository.DocumentRepository;
import com.team404.synco.drive.repository.DriveChannelRepository;
import com.team404.synco.drive.repository.FolderRepository;
import com.team404.synco.drive.util.FileTypeClassifier;
import com.team404.synco.common.constant.DriveItemType;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommonDriveService {

    private final DriveChannelRepository driveChannelRepository;
    private final FolderRepository folderRepository;
    private final DocumentRepository documentRepository;
    private final S3Uploader s3Uploader;
    private final String folderNamePrefix = "drive/";

    // 드라이브 채널 조회
    public DriveChannel getDriveChannel(Long driveChannelSeq) {
        return driveChannelRepository.findById(driveChannelSeq).orElseThrow(() -> new EntityNotFoundException("드라이브 채널을 찾을 수 없습니다: " + driveChannelSeq));
    }

    // 드라이브 아이템 목록 조회 (공통)
    public List<DriveItemDto> getDriveItems(DriveChannel driveChannel, Long parentFolderSeq) {
        List<DriveItemDto> items = new ArrayList<>();

        if (parentFolderSeq == null) {
            List<Folder> rootFolders = folderRepository.findByDriveChannelDriveChannelSeqAndParentFolderSeq(driveChannel.getDriveChannelSeq(), 0L);
            List<Document> rootDocuments = documentRepository.findByFolderDriveChannelDriveChannelSeqAndFolderParentFolderSeq(driveChannel.getDriveChannelSeq(), 0L);

            items.addAll(convertFoldersToDto(rootFolders));
            items.addAll(convertDocumentsToDto(rootDocuments));
        } else {
            List<Folder> folders = folderRepository.findByParentFolderSeq(parentFolderSeq);
            List<Document> documents = documentRepository.findByFolderFolderSeq(parentFolderSeq);

            items.addAll(convertFoldersToDto(folders));
            items.addAll(convertDocumentsToDto(documents));
        }

        return items;
    }


    // 폴더 생성 (공통)
    public DriveItemDto createFolder(DriveChannel driveChannel, String folderName, Long parentFolderId) {
        long parentFolderSeq = parentFolderId != null ? parentFolderId : 0L;

        Long maxOrder = folderRepository.findMaxOrdersByParentFolderSeqAndDriveChannelSeq(
                parentFolderSeq, driveChannel.getDriveChannelSeq()).orElse(0L);

        Folder folder = Folder.builder()
                .folderName(folderName)
                .parentFolderSeq(parentFolderSeq)
                .orders(maxOrder + 1L)
                .driveChannel(driveChannel)
                .build();

        Folder savedFolder = folderRepository.save(folder);
        return convertFolderToDto(savedFolder);
    }


    // 공유문서 생성 (공통)
    public DriveItemDto createSharedDoc(Long userId, String documentName, Long parentFolderId, Boolean isLocked) {
        // 폴더 조회
        Folder folder = folderRepository.findById(parentFolderId).orElseThrow(() -> new EntityNotFoundException("폴더를 찾을 수 없습니다."));

        Document document = Document.builder()
                .documentType(DocumentType.CUSTOM)
                .documentName(documentName)
                .documentUrl("/documents/" + UUID.randomUUID() + ".txt")
                .memberSeq(userId)
                .ynLock(isLocked != null && isLocked ? YnColumn.IS_TRUE : YnColumn.IS_FALSE)
                .folder(folder)
                .build();

        Document savedDocument = documentRepository.save(document);
        return convertDocumentToDto(savedDocument);
    }


    // 파일 업로드 (공통)
    public List<DriveItemDto> uploadFiles(DriveChannel driveChannel, Long userId, List<MultipartFile> files, Long parentFolderId) {
        List<DriveItemDto> uploadedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                String fileName = file.getOriginalFilename();
                String fileUrl = s3Uploader.upload(file, folderNamePrefix + driveChannel.getDriveChannelSeq());

                Folder folder = parentFolderId != null ?
                        folderRepository.findById(parentFolderId).orElse(null) : null;

                Document document = Document.builder()
                        .documentType(DocumentType.LOCAL)
                        .documentName(fileName)
                        .documentUrl(fileUrl)
                        .memberSeq(userId)
                        .ynLock(YnColumn.IS_FALSE)
                        .folder(folder)
                        .build();

                Document savedDocument = documentRepository.save(document);
                uploadedFiles.add(convertDocumentToDto(savedDocument));

            } catch (Exception e) {
                log.error("파일 업로드 실패: {}", file.getOriginalFilename(), e);
                throw new RuntimeException("파일 업로드에 실패했습니다.", e);
            }
        }

        return uploadedFiles;
    }


    // 아이템 이동 (공통)
    public void moveItem(String itemType, Long itemId, Long newParentId) {
        if (DriveItemType.FOLDER.equals(itemType)) {
            Folder folder = folderRepository.findById(itemId).orElseThrow(() -> new EntityNotFoundException("폴더를 찾을 수 없습니다."));
            folder.updateParentFolderSeq(newParentId);

            log.info("폴더 이동 완료: folderId={}, folderName={}, newParentId={}", itemId, folder.getFolderName(), newParentId);

        } else {
            Document document = documentRepository.findById(itemId).orElseThrow(() -> new EntityNotFoundException("문서를 찾을 수 없습니다."));
            Folder newFolder = newParentId != null ? folderRepository.findById(newParentId).orElse(null) : null;
            document.updateFolder(newFolder);

            log.info("문서 이동 완료: documentId={}, documentName={}, newParentId={}", itemId, document.getDocumentName(), newParentId);
        }
    }

    // 아이템 순서 변경 (공통)
    public void reorderItem(String itemType, Long itemId, Long newOrder) {
        if (DriveItemType.FOLDER.equals(itemType)) {
            Folder folder = folderRepository.findById(itemId).orElseThrow(() -> new EntityNotFoundException("폴더를 찾을 수 없습니다."));
            folder.updateOrder(newOrder);

            log.info("폴더 순서 변경 완료: folderId={}, folderName={}, newOrder={}", itemId, folder.getFolderName(), newOrder);

        } else {
            throw new UnsupportedOperationException("문서의 순서 변경은 현재 지원하지 않습니다.");
        }
    }


    // 폴더 이름 변경 (공통)
    public DriveItemDto renameFolder(Long folderId, String newFolderName) {
        Folder folder = folderRepository.findById(folderId).orElseThrow(() -> new EntityNotFoundException("폴더를 찾을 수 없습니다."));
        folder.updateFolderName(newFolderName);

        log.info("폴더 이름 변경 완료: folderId={}, newName={}", folderId, newFolderName);

        return convertFolderToDto(folder);
    }

    // 아이템 삭제 (공통)
    public void deleteItem(String itemType, Long itemId) {
        if (DriveItemType.FOLDER.equals(itemType)) {
            deleteFolderRecursively(itemId);
        } else {
            Document document = documentRepository.findById(itemId).orElseThrow(() -> new EntityNotFoundException("문서를 찾을 수 없습니다."));
            if (document.getDocumentType() == DocumentType.LOCAL) {
                s3Uploader.delete(document.getDocumentUrl());
            }

            documentRepository.delete(document);
        }
    }

    private void deleteFolderRecursively(Long folderId) {
        folderRepository.findById(folderId).orElseThrow(() -> new EntityNotFoundException("폴더를 찾을 수 없습니다."));

        // 1. 모든 하위 폴더 ID 수집
        List<Long> allFolderIds = new ArrayList<>();
        collectAllSubFolderIds(folderId, allFolderIds);
        allFolderIds.add(folderId);

        // 2. S3 파일들 수집 및 삭제
        List<String> s3UrlsToDelete = new ArrayList<>();
        for (Long folderIdToDelete : allFolderIds) {
            List<Document> documents = documentRepository.findByFolderFolderSeq(folderIdToDelete);
            for (Document doc : documents) {
                if (doc.getDocumentType() == DocumentType.LOCAL) {
                    s3UrlsToDelete.add(doc.getDocumentUrl());
                }
            }
        }

        if (!s3UrlsToDelete.isEmpty()) {
            try {
                for (String s3Url : s3UrlsToDelete) {
                    s3Uploader.delete(s3Url);
                }
                log.info("S3 파일 삭제 완료: {} 개 파일", s3UrlsToDelete.size());
            } catch (Exception e) {
                log.error("S3 파일 삭제 실패", e);
            }
        }

        // 4. 폴더 삭제 (CASCADE로 해당 폴더의 문서들과 DocumentLine들 자동 삭제)
        Collections.reverse(allFolderIds);
        for (Long folderIdToDelete : allFolderIds) {
            folderRepository.deleteById(folderIdToDelete);
        }
        log.info("폴더 CASCADE 삭제 완료: {} 개 폴더", allFolderIds.size());
    }

    private void collectAllSubFolderIds(Long parentFolderId, List<Long> allFolderIds) {
        List<Folder> subFolders = folderRepository.findByParentFolderSeq(parentFolderId);
        for (Folder subFolder : subFolders) {
            allFolderIds.add(subFolder.getFolderSeq());
            collectAllSubFolderIds(subFolder.getFolderSeq(), allFolderIds);
        }
    }

    public List<DriveItemDto> convertFoldersToDto(List<Folder> folders) {
        return folders.stream()
                .map(this::convertFolderToDto)
                .collect(Collectors.toList());
    }

    public List<DriveItemDto> convertDocumentsToDto(List<Document> documents) {
        return documents.stream()
                .map(this::convertDocumentToDto)
                .collect(Collectors.toList());
    }

    public DriveItemDto convertFolderToDto(Folder folder) {
        FileTypeClassifier.FolderTypeInfo typeInfo = FileTypeClassifier.FolderTypeInfo.of(folder);

        return DriveItemDto.builder()
                .id(folder.getFolderSeq())
                .name(folder.getFolderName())
                .type(typeInfo.type)
                .size(typeInfo.size)
                .uploadDate(folder.getCreatedAt())
                .modifiedDate(folder.getUpdatedAt())
                .icon(typeInfo.icon)
                .parentFolderSeq(folder.getParentFolderSeq() == 0L ? null : folder.getParentFolderSeq())
                .children(new ArrayList<>())
                .build();
    }

    public DriveItemDto convertDocumentToDto(Document document) {
        FileTypeClassifier.DocumentTypeInfo typeInfo = FileTypeClassifier.DocumentTypeInfo.of(document);

        return DriveItemDto.builder()
                .id(document.getDocumentSeq())
                .name(document.getDocumentName())
                .type(typeInfo.type)
                .size(typeInfo.size)
                .uploadDate(document.getCreatedAt())
                .modifiedDate(document.getUpdatedAt())
                .icon(typeInfo.icon)
                .parentFolderSeq(document.getFolder() != null ? document.getFolder().getFolderSeq() : null)
                .isShared(typeInfo.isShared)
                .isLocked(typeInfo.isLocked)
                .content("")
                .documentUrl(document.getDocumentUrl())
                .documentType(document.getDocumentType())
                .memberSeq(document.getMemberSeq())
                .build();
    }
}
