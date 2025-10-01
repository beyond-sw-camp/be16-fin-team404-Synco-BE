package com.team404.synco.drive.service;

import com.team404.synco.common.constant.DocumentType;
import com.team404.synco.common.constant.YnColumn;
import com.team404.synco.drive.dto.*;
import com.team404.synco.drive.entity.*;
import com.team404.synco.drive.repository.DocumentRepository;
import com.team404.synco.drive.repository.DriveChannelRepository;
import com.team404.synco.drive.repository.FolderRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
@Transactional
@RequiredArgsConstructor
public class DriveService {

    private final DriveChannelRepository driveChannelRepository;
    private final FolderRepository folderRepository;
    private final DocumentRepository documentRepository;
    
    private static final String UPLOAD_DIR = "uploads/";

    /**
     * 드라이브 아이템 목록 조회
     */
    public List<DriveItemDto> getDriveItems(Long driveChannelSeq, Long parentFolderId, 
                                          String searchQuery, String sortBy, String sortOrder) {
        List<DriveItemDto> items = new ArrayList<>();
        
        if (parentFolderId == null) {
            // 루트 폴더의 아이템들 조회
            List<Folder> rootFolders = folderRepository.findByDriveChannelDriveChannelSeqAndParentFolderSeq(driveChannelSeq, 0L);
            List<Document> rootDocuments = documentRepository.findByFolderDriveChannelDriveChannelSeqAndFolderParentFolderSeq(driveChannelSeq, 0L);
            
            items.addAll(convertFoldersToDto(rootFolders));
            items.addAll(convertDocumentsToDto(rootDocuments));
        } else {
            // 특정 폴더의 아이템들 조회
            List<Folder> folders = folderRepository.findByParentFolderSeq(parentFolderId);
            List<Document> documents = documentRepository.findByFolderFolderSeq(parentFolderId);
            
            items.addAll(convertFoldersToDto(folders));
            items.addAll(convertDocumentsToDto(documents));
        }
        
        // 검색 필터링
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            items = items.stream()
                .filter(item -> item.getName().toLowerCase().contains(searchQuery.toLowerCase()))
                .collect(Collectors.toList());
        }
        
        // 정렬
        items = sortItems(items, sortBy, sortOrder);
        
        return items;
    }

     // 폴더 생성
    public DriveItemDto createFolder(CreateFolderRequest request) {
        Folder folder = Folder.builder()
            .folderName(request.getFolderName())
            .parentFolderSeq(request.getParentFolderId() != null ? request.getParentFolderId() : 0L)
            .orders(0L)
            .driveChannel(driveChannelRepository.findById(request.getDriveChannelSeq())
                .orElseThrow(() -> new EntityNotFoundException("드라이브 채널을 찾을 수 없습니다.")))
            .build();
        
        Folder savedFolder = folderRepository.save(folder);
        return convertFolderToDto(savedFolder);
    }

    /**
     * 공유문서 생성
     */
    public DriveItemDto createSharedDoc(CreateSharedDocRequest request, Long userId) {
        Folder folder = folderRepository.findById(request.getParentFolderId())
            .orElseThrow(() -> new EntityNotFoundException("폴더를 찾을 수 없습니다."));
        
        Document document = Document.builder()
            .documentType(DocumentType.CUSTOM)
            .documentName(request.getDocumentName())
            .documentUrl("") // 공유문서는 URL이 없음
            .memberSeq(userId) // X-User-Id 헤더에서 받은 사용자 ID
            .ynLock(request.getIsLocked() != null && request.getIsLocked() ? YnColumn.IS_TRUE : YnColumn.IS_FALSE)
            .folder(folder)
            .build();
        
        Document savedDocument = documentRepository.save(document);
        
        // 공유문서 내용을 DocumentLine에 저장
        if (request.getContent() != null && !request.getContent().trim().isEmpty()) {
            // DocumentLine 저장 로직은 DocumentService에서 처리
            log.info("공유문서 내용 저장: {}", request.getContent());
        }
        
        return convertDocumentToDto(savedDocument);
    }

    /**
     * 파일 업로드
     */
    public List<DriveItemDto> uploadFiles(FileUploadRequest request, Long userId) {
        try {
            List<DriveItemDto> uploadedFiles = new ArrayList<>();
            
            for (MultipartFile file : request.getFiles()) {
                String fileName = file.getOriginalFilename();
                String fileExtension = getFileExtension(fileName);
                String uniqueFileName = UUID.randomUUID().toString() + "." + fileExtension;
                
                // 파일 저장
                Path uploadPath = Paths.get(UPLOAD_DIR + uniqueFileName);
                Files.createDirectories(uploadPath.getParent());
                Files.write(uploadPath, file.getBytes());
                
                // Document 엔티티 생성
                Folder folder = request.getParentFolderId() != null ? 
                    folderRepository.findById(request.getParentFolderId()).orElse(null) : null;
                
                Document document = Document.builder()
                    .documentType(DocumentType.LOCAL)
                    .documentName(fileName)
                    .documentUrl(uniqueFileName)
                    .memberSeq(userId) // X-User-Id 헤더에서 받은 사용자 ID
                    .ynLock(YnColumn.IS_FALSE)
                    .folder(folder)
                    .build();
                
                Document savedDocument = documentRepository.save(document);
                uploadedFiles.add(convertDocumentToDto(savedDocument));
            }
            
            return uploadedFiles;
        } catch (IOException e) {
            log.error("파일 업로드 실패", e);
            throw new IllegalStateException("파일 업로드에 실패했습니다.", e);
        }
    }

    /**
     * 아이템 이동
     */
    public void moveItem(MoveItemRequest request) {
        if ("folder".equals(request.getItemType())) {
            Folder folder = folderRepository.findById(request.getItemId())
                .orElseThrow(() -> new EntityNotFoundException("폴더를 찾을 수 없습니다."));
            folder.updateParentFolderSeq(request.getNewParentId());
            folderRepository.save(folder);
        } else if ("document".equals(request.getItemType())) {
            Document document = documentRepository.findById(request.getItemId())
                .orElseThrow(() -> new EntityNotFoundException("문서를 찾을 수 없습니다."));
            Folder newFolder = request.getNewParentId() != null ? 
                folderRepository.findById(request.getNewParentId()).orElse(null) : null;
            document.updateFolder(newFolder);
            documentRepository.save(document);
        }
    }

    /**
     * 파일 다운로드
     */
    public ResponseEntity<byte[]> downloadFile(Long documentSeq) {
        try {
            Document document = documentRepository.findById(documentSeq)
                .orElseThrow(() -> new EntityNotFoundException("문서를 찾을 수 없습니다."));
            
            Path filePath = Paths.get(UPLOAD_DIR + document.getDocumentUrl());
            byte[] fileContent = Files.readAllBytes(filePath);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", document.getDocumentName());
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(fileContent);
        } catch (IOException e) {
            log.error("파일 다운로드 실패", e);
            throw new IllegalStateException("파일 다운로드에 실패했습니다.", e);
        }
    }

    /**
     * 아이템 삭제
     */
    public void deleteItem(String itemType, Long itemId) {
        if ("folder".equals(itemType)) {
            folderRepository.deleteById(itemId);
        } else if ("document".equals(itemType)) {
            documentRepository.deleteById(itemId);
        }
    }

    /**
     * 드라이브 채널 생성
     */
    // TODO: Workspace와 연동 필요
    public DriveItemDto createDriveChannel(String driveChannelName, Long workspaceSeq) {
        DriveChannel channel = DriveChannel.builder()
            .driveChannelName(driveChannelName)
            .workspaceSeq(workspaceSeq)
            .build();
        
        DriveChannel savedChannel = driveChannelRepository.save(channel);
        return convertDriveChannelToDto(savedChannel);
    }

    // Helper Methods

    private List<DriveItemDto> convertFoldersToDto(List<Folder> folders) {
        return folders.stream()
            .map(this::convertFolderToDto)
            .collect(Collectors.toList());
    }

    private List<DriveItemDto> convertDocumentsToDto(List<Document> documents) {
        return documents.stream()
            .map(this::convertDocumentToDto)
            .collect(Collectors.toList());
    }

    private DriveItemDto convertFolderToDto(Folder folder) {
        return DriveItemDto.builder()
            .id(folder.getFolderSeq())
            .name(folder.getFolderName())
            .type("folder")
            .size("-")
            .uploadDate(folder.getCreatedAt())
            .modifiedDate(folder.getUpdatedAt())
            .icon("mdi-folder")
            .parentId(folder.getParentFolderSeq() == 0L ? null : folder.getParentFolderSeq())
            .children(new ArrayList<>())
            .build();
    }

    private DriveItemDto convertDocumentToDto(Document document) {
        boolean isShared = DocumentType.CUSTOM.equals(document.getDocumentType());
        
        return DriveItemDto.builder()
            .id(document.getDocumentSeq())
            .name(document.getDocumentName())
            .type(isShared ? "shared-doc" : "file")
            .size(isShared ? "-" : "0 KB") // 실제 파일 크기 계산 필요
            .uploadDate(document.getCreatedAt())
            .modifiedDate(document.getUpdatedAt())
            .icon(isShared ? "mdi-file-document-multiple" : getFileIcon(document.getDocumentName()))
            .parentId(document.getFolder() != null ? document.getFolder().getFolderSeq() : null)
            .isShared(isShared)
            .isLocked(YnColumn.IS_TRUE.equals(document.getYnLock()))
            .content("") // DocumentLine에서 조회 필요
            .documentUrl(document.getDocumentUrl())
            .documentType(document.getDocumentType())
            .memberSeq(document.getMemberSeq())
            .build();
    }

    private DriveItemDto convertDriveChannelToDto(DriveChannel channel) {
        return DriveItemDto.builder()
            .id(channel.getDriveChannelSeq())
            .name(channel.getDriveChannelName())
            .type("channel")
            .size("-")
            .uploadDate(channel.getCreatedAt())
            .modifiedDate(channel.getUpdatedAt())
            .icon("mdi-folder-multiple")
            .parentId(null)
            .children(new ArrayList<>())
            .build();
    }

    private List<DriveItemDto> sortItems(List<DriveItemDto> items, String sortBy, String sortOrder) {
        return items.stream()
            .sorted((a, b) -> {
                int comparison = 0;
                switch (sortBy) {
                    case "name":
                        comparison = a.getName().compareTo(b.getName());
                        break;
                    case "date":
                        comparison = a.getModifiedDate().compareTo(b.getModifiedDate());
                        break;
                    case "size":
                        comparison = a.getSize().compareTo(b.getSize());
                        break;
                    case "type":
                        comparison = a.getType().compareTo(b.getType());
                        break;
                }
                return "desc".equals(sortOrder) ? -comparison : comparison;
            })
            .collect(Collectors.toList());
    }

    private String getFileIcon(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        switch (extension) {
            case "pdf": return "mdi-file-pdf";
            case "doc":
            case "docx": return "mdi-file-word";
            case "xls":
            case "xlsx": return "mdi-file-excel";
            case "ppt":
            case "pptx": return "mdi-file-powerpoint";
            case "jpg":
            case "jpeg":
            case "png":
            case "gif": return "mdi-file-image";
            case "txt": return "mdi-file-document";
            default: return "mdi-file";
        }
    }

    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

}
