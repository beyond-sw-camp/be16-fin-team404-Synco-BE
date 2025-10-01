package com.team404.synco.drive.service;

import com.team404.synco.common.constant.DocumentType;
import com.team404.synco.common.constant.WorkSpaceType;
import com.team404.synco.common.constant.YnColumn;
import com.team404.synco.drive.dto.DriveItemDto;
import com.team404.synco.drive.entity.Document;
import com.team404.synco.drive.entity.DriveChannel;
import com.team404.synco.drive.entity.Folder;
import com.team404.synco.drive.repository.DocumentRepository;
import com.team404.synco.drive.repository.DriveChannelRepository;
import com.team404.synco.drive.repository.FolderRepository;
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
    
    private static final String UPLOAD_DIR = "uploads/";

    // 드라이브 채널 조회
    public DriveChannel getDriveChannel(Long driveChannelSeq) {
        return driveChannelRepository.findById(driveChannelSeq)
            .orElseThrow(() -> new RuntimeException("드라이브 채널을 찾을 수 없습니다: " + driveChannelSeq));
    }

    // 개인 드라이브 채널 조회
    public DriveChannel getPersonalDriveChannel(Long userId) {
        return driveChannelRepository.findByWorkspaceTypeAndWorkspaceSeq(WorkSpaceType.INDIVIDUAL, userId)
            .orElseThrow(() -> new RuntimeException("개인 드라이브를 찾을 수 없습니다."));
    }


    // 드라이브 아이템 목록 조회 (공통)
    public List<DriveItemDto> getDriveItems(DriveChannel driveChannel, Long parentFolderId, 
                                          String searchQuery, String sortBy, String sortOrder) {
        List<DriveItemDto> items = new ArrayList<>();
        
        if (parentFolderId == null) {
            // 루트 폴더의 아이템들 조회
            List<Folder> rootFolders = folderRepository.findByDriveChannelDriveChannelSeqAndParentFolderSeq(driveChannel.getDriveChannelSeq(), 0L);
            List<Document> rootDocuments = documentRepository.findByFolderDriveChannelDriveChannelSeqAndFolderParentFolderSeq(driveChannel.getDriveChannelSeq(), 0L);
            
            items.addAll(convertFoldersToDto(rootFolders, driveChannel.getWorkspaceType()));
            items.addAll(convertDocumentsToDto(rootDocuments, driveChannel.getWorkspaceType()));
        } else {
            // 특정 폴더의 아이템들 조회
            List<Folder> folders = folderRepository.findByParentFolderSeq(parentFolderId);
            List<Document> documents = documentRepository.findByFolderFolderSeq(parentFolderId);
            
            items.addAll(convertFoldersToDto(folders, driveChannel.getWorkspaceType()));
            items.addAll(convertDocumentsToDto(documents, driveChannel.getWorkspaceType()));
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
            .orElseThrow(() -> new RuntimeException("폴더를 찾을 수 없습니다."));
        
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
                String fileUrl = saveFile(file);
                
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
                
            } catch (IOException e) {
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
                .orElseThrow(() -> new RuntimeException("폴더를 찾을 수 없습니다."));
            
            folder.updateParentFolderSeq(newParentId);
            folderRepository.save(folder);
            
        } else if ("document".equals(itemType)) {
            Document document = documentRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("문서를 찾을 수 없습니다."));
            
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
                .orElseThrow(() -> new RuntimeException("폴더를 찾을 수 없습니다."));
            
            folderRepository.delete(folder);
            
        } else if ("document".equals(itemType)) {
            Document document = documentRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("문서를 찾을 수 없습니다."));
            
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
        // 개인 드라이브와 팀 드라이브에 따른 아이콘/색상 구분
        String icon = workspaceType == WorkSpaceType.INDIVIDUAL ? "mdi-folder-account" : "mdi-folder";

        return DriveItemDto.builder()
            .id(folder.getFolderSeq())
            .name(folder.getFolderName())
            .type("folder")
            .size("-")
            .uploadDate(folder.getCreatedAt())
            .modifiedDate(folder.getUpdatedAt())
            .icon(icon)
            .parentId(folder.getParentFolderSeq() == 0L ? null : folder.getParentFolderSeq())
            .children(new ArrayList<>())
            .build();
    }

    public DriveItemDto convertDocumentToDto(Document document, WorkSpaceType workspaceType) {
        boolean isShared = DocumentType.CUSTOM.equals(document.getDocumentType());

        return DriveItemDto.builder()
            .id(document.getDocumentSeq())
            .name(document.getDocumentName())
            .type(isShared ? "shared-doc" : "file")
            .size(isShared ? "-" : "0 KB")
            .uploadDate(document.getCreatedAt())
            .modifiedDate(document.getUpdatedAt())
            .icon(isShared ? "mdi-file-document-multiple" : getFileIcon(document.getDocumentName()))
            .parentId(document.getFolder() != null ? document.getFolder().getFolderSeq() : null)
            .isShared(isShared)
            .isLocked(YnColumn.IS_TRUE.equals(document.getYnLock()))
            .content("")
            .documentUrl(document.getDocumentUrl())
            .documentType(document.getDocumentType())
            .memberSeq(document.getMemberSeq())
            .build();
    }

    public DriveItemDto convertDriveChannelToDto(DriveChannel channel) {
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

    public List<DriveItemDto> sortItems(List<DriveItemDto> items, String sortBy, String sortOrder) {
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

    private String saveFile(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIR + fileName);
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, file.getBytes());
        return UPLOAD_DIR + fileName;
    }
}
