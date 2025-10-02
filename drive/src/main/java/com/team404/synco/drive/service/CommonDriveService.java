package com.team404.synco.drive.service;

import com.team404.synco.common.constant.DocumentType;
import com.team404.synco.common.constant.WorkSpaceType;
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
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

    // 드라이브 채널 조회
    public DriveChannel getDriveChannel(Long driveChannelSeq) {
        return driveChannelRepository.findById(driveChannelSeq).orElseThrow(() -> new EntityNotFoundException("드라이브 채널을 찾을 수 없습니다: " + driveChannelSeq));
    }

    // 개인 드라이브 채널 조회
    public DriveChannel getPersonalDriveChannel(Long driveChannelSeq) {
        return driveChannelRepository.findById(driveChannelSeq).orElseThrow(() -> new EntityNotFoundException("개인 드라이브를 찾을 수 없습니다."));
    }


    // 드라이브 아이템 목록 조회 (공통)
    public List<DriveItemDto> getDriveItems(DriveChannel driveChannel, Long parentFolderSeq) {
        List<DriveItemDto> items = new ArrayList<>();
        
        if (parentFolderSeq == null) {
            List<Folder> rootFolders = folderRepository.findByDriveChannelDriveChannelSeqAndParentFolderSeq(driveChannel.getDriveChannelSeq(), 0L);
            List<Document> rootDocuments = documentRepository.findByFolderDriveChannelDriveChannelSeqAndFolderParentFolderSeq(driveChannel.getDriveChannelSeq(), 0L);
            
            items.addAll(convertFoldersToDto(rootFolders, driveChannel.getWorkspaceType()));
            items.addAll(convertDocumentsToDto(rootDocuments, driveChannel.getWorkspaceType()));
        } else {
            List<Folder> folders = folderRepository.findByParentFolderSeq(parentFolderSeq);
            List<Document> documents = documentRepository.findByFolderFolderSeq(parentFolderSeq);
            
            items.addAll(convertFoldersToDto(folders, driveChannel.getWorkspaceType()));
            items.addAll(convertDocumentsToDto(documents, driveChannel.getWorkspaceType()));
        }
        
        return items;
    }


    // 폴더 생성 (공통)
    public DriveItemDto createFolder(DriveChannel driveChannel, String folderName, Long parentFolderId) {
        Folder folder = Folder.builder()
            .folderName(folderName)
            .parentFolderSeq(parentFolderId != null ? parentFolderId : 0L)
            .orders(1L)
            .driveChannel(driveChannel)
            .build();
        
        Folder savedFolder = folderRepository.save(folder);
        return convertFolderToDto(savedFolder, driveChannel.getWorkspaceType());
    }


    // 공유문서 생성 (공통)
    public DriveItemDto createSharedDoc(DriveChannel driveChannel, Long userId, String documentName, 
                                       Long parentFolderId, Boolean isLocked, String content) {
        // 폴더 조회
        Folder folder = folderRepository.findById(parentFolderId)
            .orElseThrow(() -> new EntityNotFoundException("폴더를 찾을 수 없습니다."));
        
        Document document = Document.builder()
            .documentType(DocumentType.CUSTOM)
            .documentName(documentName)
            .documentUrl("/documents/" + UUID.randomUUID() + ".docx")
            .memberSeq(userId)
            .ynLock(isLocked != null && isLocked ? YnColumn.IS_TRUE : YnColumn.IS_FALSE)
            .folder(folder)
            .build();
        
        Document savedDocument = documentRepository.save(document);
        return convertDocumentToDto(savedDocument, driveChannel.getWorkspaceType());
    }


    // 파일 업로드 (공통)
    public List<DriveItemDto> uploadFiles(DriveChannel driveChannel, Long userId, List<MultipartFile> files, Long parentFolderId) {
        List<DriveItemDto> uploadedFiles = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                String fileName = file.getOriginalFilename();
                // S3에 파일 업로드
                String fileUrl = s3Uploader.upload(file, "drive/" + driveChannel.getDriveChannelSeq());
                
                // 폴더 조회
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
                uploadedFiles.add(convertDocumentToDto(savedDocument, driveChannel.getWorkspaceType()));
                
            } catch (Exception e) {
                log.error("파일 업로드 실패: {}", file.getOriginalFilename(), e);
                throw new RuntimeException("파일 업로드에 실패했습니다.", e);
            }
        }
        
        return uploadedFiles;
    }


    // 아이템 이동 (공통)
    public void moveItem(String itemType, Long itemId, Long newParentId) {
        if ("folder".equals(itemType)) {
            Folder folder = folderRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("폴더를 찾을 수 없습니다."));
            
            folder.updateParentFolderSeq(newParentId);
            folderRepository.save(folder);
            
        } else if ("document".equals(itemType)) {
            Document document = documentRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("문서를 찾을 수 없습니다."));
            
            Folder newFolder = newParentId != null ? 
                folderRepository.findById(newParentId).orElse(null) : null;
            document.updateFolder(newFolder);
            documentRepository.save(document);
        }
    }


    // 아이템 삭제 (공통)
    public void deleteItem(String itemType, Long itemId) {
        if ("folder".equals(itemType)) {
            Folder folder = folderRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("폴더를 찾을 수 없습니다."));
            
            folderRepository.delete(folder);
            
        } else if ("document".equals(itemType)) {
            Document document = documentRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("문서를 찾을 수 없습니다."));
            
            documentRepository.delete(document);
        }
    }

    // Helper Methods
    public List<DriveItemDto> convertFoldersToDto(List<Folder> folders, WorkSpaceType workspaceType) {
        return folders.stream()
            .map(folder -> convertFolderToDto(folder, workspaceType))
            .collect(Collectors.toList());
    }

    public List<DriveItemDto> convertDocumentsToDto(List<Document> documents, WorkSpaceType workspaceType) {
        return documents.stream()
            .map(document -> convertDocumentToDto(document, workspaceType))
            .collect(Collectors.toList());
    }

    public DriveItemDto convertFolderToDto(Folder folder, WorkSpaceType workspaceType) {
        FileTypeClassifier.FolderTypeInfo typeInfo = FileTypeClassifier.FolderTypeInfo.of(folder);
        String icon = workspaceType == WorkSpaceType.INDIVIDUAL ? "mdi-folder-account" : typeInfo.icon;

        return DriveItemDto.builder()
            .id(folder.getFolderSeq())
            .name(folder.getFolderName())
            .type(typeInfo.type)
            .size(typeInfo.size)
            .uploadDate(folder.getCreatedAt())
            .modifiedDate(folder.getUpdatedAt())
            .icon(icon)
            .parentFolderSeq(folder.getParentFolderSeq() == 0L ? null : folder.getParentFolderSeq())
            .children(new ArrayList<>())
            .build();
    }

    public DriveItemDto convertDocumentToDto(Document document, WorkSpaceType workspaceType) {
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

    public DriveItemDto convertDriveChannelToDto(DriveChannel channel) {
        FileTypeClassifier.ChannelTypeInfo typeInfo = FileTypeClassifier.ChannelTypeInfo.of();
        
        return DriveItemDto.builder()
            .id(channel.getDriveChannelSeq())
            .name(channel.getDriveChannelName())
            .type(typeInfo.type)
            .size(typeInfo.size)
            .uploadDate(channel.getCreatedAt())
            .modifiedDate(channel.getUpdatedAt())
            .icon(typeInfo.icon)
            .parentFolderSeq(null)
            .children(new ArrayList<>())
            .build();
    }



}
