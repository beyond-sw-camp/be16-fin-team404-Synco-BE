package com.team404.synco.drive.service;

import com.team404.synco.common.constant.DocumentType;
import com.team404.synco.common.constant.DriveItemType;
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
import com.team404.synco.drive.specification.DriveItemSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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

    // 개인 드라이브 채널 조회
    public DriveChannel getPersonalDriveChannel(Long driveChannelSeq) {
        DriveChannel channel = driveChannelRepository.findById(driveChannelSeq).orElseThrow(() -> new EntityNotFoundException("드라이브 채널을 찾을 수 없습니다: " + driveChannelSeq));
        
        // 개인 드라이브 채널인지 확인
        if (channel.getWorkspaceType() != WorkSpaceType.INDIVIDUAL) {
            throw new IllegalArgumentException("개인 드라이브 채널이 아닙니다: " + driveChannelSeq);
        }
        
        return channel;
    }

    // 프로젝트 드라이브 채널 조회
    public DriveChannel getProjectDriveChannel(Long driveChannelSeq) {
        DriveChannel channel = driveChannelRepository.findById(driveChannelSeq).orElseThrow(() -> new EntityNotFoundException("드라이브 채널을 찾을 수 없습니다: " + driveChannelSeq));
        
        // 프로젝트 드라이브 채널인지 확인
        if (channel.getWorkspaceType()!= WorkSpaceType.PROJECT) {
            throw new IllegalArgumentException("프로젝트 드라이브 채널이 아닙니다: " + driveChannelSeq);
        }
        
        return channel;
    }

    // 드라이브 아이템 목록 조회
    public Page<DriveItemDto> getDriveItems(DriveChannel driveChannel,
                                            Long parentFolderSeq,
                                            Pageable pageable,
                                            String sortBy,
                                            String sortOrder) {
        
        Pageable sortedPageable = createSortedPageable(pageable, sortBy, sortOrder);
        
        Page<Folder> folders = folderRepository.findAll(DriveItemSpecification.folderByDriveChannelAndParent(driveChannel.getDriveChannelSeq(), parentFolderSeq), sortedPageable);
        Page<Document> documents = documentRepository.findAll(DriveItemSpecification.documentByDriveChannelAndParent(driveChannel.getDriveChannelSeq(), parentFolderSeq), sortedPageable);
        
        List<DriveItemDto> allItems = new ArrayList<>();
        allItems.addAll(folders.getContent().stream().map(DriveItemDto::fromFolder).collect(Collectors.toList()));
        allItems.addAll(documents.getContent().stream().map(DriveItemDto::fromDocument).collect(Collectors.toList()));
        
        long totalElements = folders.getTotalElements() + documents.getTotalElements();
        
        return new PageImpl<>(allItems, pageable, totalElements);
    }
    
    private Pageable createSortedPageable(Pageable pageable, String sortBy, String sortOrder) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return pageable;
        }
        
        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        
        List<Sort.Order> orders = new ArrayList<>();
        
        switch (sortBy.toLowerCase()) {
            case "name":
                orders.add(Sort.Order.by("folderName").with(direction));
                orders.add(Sort.Order.by("documentName").with(direction));
                orders.add(Sort.Order.by("updatedAt").with(Sort.Direction.DESC));
                break;
                
            case "date":
                orders.add(Sort.Order.by("updatedAt").with(direction));
                orders.add(Sort.Order.by("folderName").with(Sort.Direction.ASC));
                orders.add(Sort.Order.by("documentName").with(Sort.Direction.ASC));
                break;
                
            default:
                return pageable;
        }
        
        return PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            Sort.by(orders)
        );
    }
    
    // 폴더 생성
    public DriveItemDto createFolder(DriveChannel driveChannel, String folderName, Long parentFolderId) {

        // 같은 드라이브 채널 내에서 같은 위치에 같은 이름의 폴더가 있는지 확인
        if(parentFolderId != null) {
            // 하위 폴더 중복 검증
            if(folderRepository.findByFolderNameAndFolderSeqNotAndDriveChannelAndParentFolderSeq(folderName, null, driveChannel, parentFolderId).isPresent()) {
                throw new IllegalArgumentException("해당 폴더 위치에 같은 이름의 폴더가 이미 존재합니다.");
            }
        } else {
            // 최상위 폴더 중복 검증
            if(folderRepository.findByFolderNameAndFolderSeqNotAndDriveChannelAndParentFolderSeq(folderName, null, driveChannel, null).isPresent()) {
                throw new IllegalArgumentException("최상위에 같은 이름의 폴더가 이미 존재합니다.");
            }
        }

        // 부모폴더가 있는 경우에만 존재 여부 확인
        if(parentFolderId != null){
            folderRepository.findByFolderSeqAndDriveChannelDriveChannelSeq(parentFolderId, driveChannel.getDriveChannelSeq())
                .orElseThrow(() -> new EntityNotFoundException("부모 폴더를 찾을 수 없습니다. 폴더 ID: " + parentFolderId));
        }

        // 최대 orders 조회
        long maxOrder;
        if(parentFolderId != null) {
            maxOrder = folderRepository.findMaxOrdersByParentFolderSeqAndDriveChannelSeq(parentFolderId, driveChannel.getDriveChannelSeq()).orElse(0L);
        } else {
            maxOrder = folderRepository.findMaxOrdersByParentFolderSeqIsNullAndDriveChannelSeq(driveChannel.getDriveChannelSeq()).orElse(0L);
        }

        Folder folder = Folder.builder()
                .folderName(folderName)
                .parentFolderSeq(parentFolderId)  // null이면 최상위 폴더
                .orders(maxOrder + 1L)
                .driveChannel(driveChannel)
                .build();

        Folder savedFolder = folderRepository.save(folder);
        return DriveItemDto.fromFolder(savedFolder);
    }


    // 공유문서 생성
    public DriveItemDto createSharedDoc(DriveChannel driveChannel, Long userId, String documentName, Long parentFolderId, Boolean isLocked) {

        // 같은 드라이브 채널 내에서 같은 위치에 같은 이름의 문서가 있는지 확인
        if(parentFolderId != null) {
            // 폴더 내 문서 중복 검증
            if(documentRepository.findByDocumentNameAndDocumentSeqNotAndFolderDriveChannelAndFolderFolderSeq(documentName, null, driveChannel, parentFolderId).isPresent()) {
                throw new IllegalArgumentException("해당 폴더 위치에 같은 이름의 문서가 이미 존재합니다.");
            }
        } else {
            // 최상위 문서 중복 검증
            List<Document> existingDocs = documentRepository.findAll().stream()
                .filter(doc -> doc.getDocumentName().equals(documentName))
                .filter(doc -> doc.getFolder() == null)
                .filter(doc -> doc.getDriveChannel().getDriveChannelSeq().equals(driveChannel.getDriveChannelSeq()))
                .toList();
            
            if (!existingDocs.isEmpty()) {
                throw new IllegalArgumentException("최상위에 같은 이름의 문서가 이미 존재합니다.");
            }
        }

        // 부모폴더가 있는 경우에만 폴더 설정
        Folder folder = null;
        if(parentFolderId != null){
            folder = folderRepository.findByFolderSeqAndDriveChannelDriveChannelSeq(parentFolderId, driveChannel.getDriveChannelSeq())
                .orElseThrow(() -> new EntityNotFoundException("부모 폴더를 찾을 수 없습니다. 폴더 ID: " + parentFolderId));
        }

        Document document = Document.builder()
                .documentType(DocumentType.CUSTOM)
                .documentName(documentName)
                .documentUrl("/documents/" + UUID.randomUUID() + ".txt")
                .memberSeq(userId)
                .ynLock(isLocked != null && isLocked ? YnColumn.IS_TRUE : YnColumn.IS_FALSE)
                .folder(folder)
                .driveChannel(driveChannel)
                .build();

        Document savedDocument = documentRepository.save(document);
        return DriveItemDto.fromDocument(savedDocument);
    }


    // 파일 업로드
    public List<DriveItemDto> uploadFiles(DriveChannel driveChannel, Long userId, List<MultipartFile> files, Long parentFolderId) {
        List<DriveItemDto> uploadedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                String fileName = file.getOriginalFilename();
                String fileUrl = s3Uploader.upload(file, folderNamePrefix + driveChannel.getDriveChannelSeq());

                // 같은 위치에 같은 이름의 파일이 있는지 확인
                if(parentFolderId != null) {
                    // 폴더 내 파일 중복 검증
                    if(documentRepository.findByDocumentNameAndDocumentSeqNotAndFolderDriveChannelAndFolderFolderSeq(fileName, null, driveChannel, parentFolderId).isPresent()) {
                        throw new IllegalArgumentException("해당 폴더 위치에 같은 이름의 파일이 이미 존재합니다: " + fileName);
                    }
                } else {
                    // 최상위 파일 중복 검증
                    if(documentRepository.findTopLevelDocumentByNameAndChannel(fileName, null, driveChannel.getDriveChannelSeq()).isPresent()) {
                        throw new IllegalArgumentException("최상위에 같은 이름의 파일이 이미 존재합니다: " + fileName);
                    }
                }

                // 부모폴더가 있는 경우에만 폴더 설정
                Folder folder = null;
                if(parentFolderId != null){
                    folder = folderRepository.findByFolderSeqAndDriveChannelDriveChannelSeq(parentFolderId, driveChannel.getDriveChannelSeq())
                        .orElseThrow(() -> new EntityNotFoundException("부모 폴더를 찾을 수 없습니다. 폴더 ID: " + parentFolderId));
                }

                Document document = Document.builder()
                        .documentType(DocumentType.LOCAL)
                        .documentName(fileName)
                        .documentUrl(fileUrl)
                        .memberSeq(userId)
                        .ynLock(YnColumn.IS_FALSE)
                        .folder(folder)
                        .driveChannel(driveChannel)
                        .build();

                Document savedDocument = documentRepository.save(document);
                uploadedFiles.add(DriveItemDto.fromDocument(savedDocument));

            } catch (Exception e) {
                throw new MultipartException("파일 업로드 중 예상치 못한 오류가 발생했습니다: " + file.getOriginalFilename(), e);
            }
        }

        return uploadedFiles;
    }


    // 아이템 이동
    public void moveItem(String itemType, Long itemId, Long newParentId) {
        if (DriveItemType.FOLDER.equals(itemType)) {
            moveFolderWithOrderAdjustment(itemId, newParentId);
        } else {
            Document document = documentRepository.findById(itemId).orElseThrow(() -> new EntityNotFoundException("문서를 찾을 수 없습니다."));
            
            // 같은 드라이브 채널 내에서 같은 위치에 같은 이름의 문서가 있는지 확인 (자기 자신 제외)
            if(newParentId != null) {
                // 폴더로 이동하는 경우
                if(documentRepository.findByDocumentNameAndDocumentSeqNotAndFolderDriveChannelAndFolderFolderSeq(document.getDocumentName(), itemId, document.getDriveChannel(), newParentId).isPresent()) {
                    throw new IllegalArgumentException("해당 폴더 위치에 같은 이름의 문서가 이미 존재합니다.");
                }
            } else {
                // 최상위로 이동하는 경우
                if(documentRepository.findTopLevelDocumentByNameAndChannel(document.getDocumentName(), itemId, document.getDriveChannel().getDriveChannelSeq()).isPresent()) {
                    throw new IllegalArgumentException("최상위에 같은 이름의 문서가 이미 존재합니다.");
                }
            }

            Folder newFolder = null;
            if(newParentId != null) {
                newFolder = folderRepository.findByFolderSeqAndDriveChannelDriveChannelSeq(newParentId, document.getDriveChannel().getDriveChannelSeq())
                    .orElseThrow(() -> new EntityNotFoundException("이동할 부모 폴더를 찾을 수 없습니다. 폴더 ID: " + newParentId));
            }
            document.updateFolder(newFolder);
        }
    }

    // 폴더 이동 시 순서 조정 포함
    private void moveFolderWithOrderAdjustment(Long folderId, Long newParentId) {
        Folder folder = folderRepository.findById(folderId).orElseThrow(() -> new EntityNotFoundException("폴더를 찾을 수 없습니다."));

        // 이동할 부모폴더가 있는 경우 존재 여부 확인
        if(newParentId != null) {
            folderRepository.findByFolderSeqAndDriveChannelDriveChannelSeq(newParentId, folder.getDriveChannel().getDriveChannelSeq())
                .orElseThrow(() -> new EntityNotFoundException("이동할 부모 폴더를 찾을 수 없습니다. 폴더 ID: " + newParentId));
            
            // 순환 참조 방지: 이동할 폴더가 현재 폴더의 하위 폴더인지 확인
            if (isCircularReference(folderId, newParentId)) {
                throw new IllegalArgumentException("자기 자신의 하위 폴더로는 이동할 수 없습니다.");
            }
        }

        // 현재 위치와 이동할 위치가 같은지 확인
        Long currentParentId = folder.getParentFolderSeq();
        if (Objects.equals(currentParentId, newParentId)) {
            throw new IllegalArgumentException("이미 해당 위치에 있는 폴더입니다.");
        }

        // 같은 드라이브 채널 내에서 같은 부모 폴더 하위에 같은 이름의 폴더가 있는지 확인
        if(folderRepository.findByFolderNameAndFolderSeqNotAndDriveChannelAndParentFolderSeq(folder.getFolderName(), folderId, folder.getDriveChannel(), newParentId).isPresent()) {
            throw new IllegalArgumentException("해당 폴더 위치에 같은 이름의 폴더가 이미 존재합니다.");
        }
        Long currentOrder = folder.getOrders();
        Long driveChannelSeq = folder.getDriveChannel().getDriveChannelSeq();

        // 1. 기존 위치에서 순서 조정 (현재 폴더보다 뒤에 있던 폴더들의 순서를 -1)
        if (currentParentId != null) {
            folderRepository.decrementOrdersFrom(currentParentId, driveChannelSeq, currentOrder + 1);
        } else {
            folderRepository.decrementOrdersFromTopLevel(driveChannelSeq, currentOrder + 1);
        }

        //  새 위치에서 최대 순서 조회 및 설정
        long maxOrder;
        if(newParentId != null) {
            maxOrder = folderRepository.findMaxOrdersByParentFolderSeqAndDriveChannelSeq(newParentId, driveChannelSeq).orElse(0L);
        } else {
            maxOrder = folderRepository.findMaxOrdersByParentFolderSeqIsNullAndDriveChannelSeq(driveChannelSeq).orElse(0L);
        }

        // 폴더의 부모와 순서 업데이트
        folder.updateParentFolderSeq(newParentId);
        folder.updateOrder(maxOrder + 1L);
        
        // 이동하기 전 위치의 모든 폴더 순서를 연속적으로 재정렬
        reorderFoldersSequentially(currentParentId, driveChannelSeq);
    }
    
    // 폴더 순서를 연속적으로 재정렬
    private void reorderFoldersSequentially(Long parentFolderId, Long driveChannelSeq) {
        List<Folder> folders;
        
        if (parentFolderId != null) {
            // 하위 폴더들 조회
            folders = folderRepository.findByParentFolderSeqAndDriveChannelDriveChannelSeqOrderByOrders(parentFolderId, driveChannelSeq);
        } else {
            // 최상위 폴더들 조회
            folders = folderRepository.findByParentFolderSeqIsNullAndDriveChannelDriveChannelSeqOrderByOrders(driveChannelSeq);
        }
        
        // 순서를 1부터 연속적으로 재설정
        for (int i = 0; i < folders.size(); i++) {
            Folder folder = folders.get(i);
            folder.updateOrder((long) (i + 1));
        }
    }

    // 아이템 순서 변경
    public void reorderItem(String itemType, Long itemId, Long newOrder) {
        if (DriveItemType.FOLDER.equals(itemType)) {
            reorderFolder(itemId, newOrder);
        } else {
            throw new UnsupportedOperationException("문서의 순서 변경은 현재 지원하지 않습니다.");
        }
    }

    // 폴더 순서 변경 로직
    private void reorderFolder(Long folderId, Long newOrder) {
        Folder folder = folderRepository.findById(folderId).orElseThrow(() -> new EntityNotFoundException("폴더를 찾을 수 없습니다."));

        Long currentOrder = folder.getOrders();
        Long parentFolderSeq = folder.getParentFolderSeq();
        Long driveChannelSeq = folder.getDriveChannel().getDriveChannelSeq();

        // 같은 순서로 변경하는 경우는 아무것도 하지 않음
        if (currentOrder.equals(newOrder)) {
            return;
        }

        if (currentOrder < newOrder) {
            // 뒤로 이동: 현재 순서보다 크고 새로운 순서 이하인 아이템들을 -1
            folderRepository.decrementOrdersFrom(parentFolderSeq, driveChannelSeq, newOrder);
        } else {
            // 앞으로 이동: 새로운 순서 이상인 아이템들을 +1
            folderRepository.incrementOrdersFrom(parentFolderSeq, driveChannelSeq, newOrder);
        }

        // 폴더의 순서 업데이트
        folder.updateOrder(newOrder);
    }


    // 폴더 이름 변경
    public DriveItemDto renameFolder(Long folderId, String newFolderName) {
        Folder folder = folderRepository.findById(folderId).orElseThrow(() -> new EntityNotFoundException("폴더를 찾을 수 없습니다."));

        // 같은 위치에서 폴더 이름 중복 검사
        if(folderRepository.findByFolderNameAndFolderSeqNotAndDriveChannelAndParentFolderSeq(newFolderName, folderId, folder.getDriveChannel(), folder.getParentFolderSeq()).isPresent()) {
            throw new IllegalArgumentException("해당 폴더 위치에 같은 이름의 폴더가 이미 존재합니다.");
        }

        folder.updateFolderName(newFolderName);

        return DriveItemDto.fromFolder(folder);
    }

    // 아이템 삭제
    public void deleteItem(Long userId, String itemType, Long itemId) {
        if (DriveItemType.FOLDER.equals(itemType)) {
            // 폴더 삭제 전 권한 체크 (재귀적으로 내부 모든 문서가 내가 올린 것인지 확인)
            validateFolderDeletePermission(userId, itemId);
            deleteFolderRecursively(itemId);
        } else {
            Document document = documentRepository.findById(itemId).orElseThrow(() -> new EntityNotFoundException("문서를 찾을 수 없습니다."));
            // 문서 삭제 권한 체크
            if (document.getMemberSeq() != userId) {
                throw new SecurityException("자신이 생성한 문서만 삭제할 수 있습니다.");
            }
            
            if (document.getDocumentType() == DocumentType.LOCAL) {
                s3Uploader.delete(document.getDocumentUrl());
            }

            documentRepository.delete(document);
        }
    }
    
    // 폴더 삭제 권한 검증
    private void validateFolderDeletePermission(Long userId, Long folderId) {
        Set<Long> visitedFolders = new HashSet<>();
        validateFolderDeletePermissionRecursive(userId, folderId, visitedFolders);
    }
    
    // 폴더 삭제 권한 검증 (재귀, 방문 추적)
    private void validateFolderDeletePermissionRecursive(Long userId, Long folderId, Set<Long> visitedFolders) {
        // 이미 방문한 폴더라면 스킵
        if (visitedFolders.contains(folderId)) {
            return;
        }
        
        // 현재 폴더를 방문한 것으로 표시
        visitedFolders.add(folderId);
        
        // 1. 현재 폴더의 모든 문서 확인
        List<Document> documentsInFolder = documentRepository.findByFolderFolderSeq(folderId);
        for (Document document : documentsInFolder) {
            if(document.getMemberSeq() != userId) {
                throw new SecurityException("자신이 생성한 문서가 아닌 폴더는 삭제할 수 없습니다.");
            }
        }

        // 하위 폴더들 재귀적으로 확인
        List<Folder> subFolders = folderRepository.findByParentFolderSeq(folderId);
        for (Folder subFolder : subFolders) {
            validateFolderDeletePermissionRecursive(userId, subFolder.getFolderSeq(), visitedFolders);
        }
    }

    private void deleteFolderRecursively(Long folderId) {
        Folder folderToDelete = folderRepository.findById(folderId).orElseThrow(() -> new EntityNotFoundException("폴더를 찾을 수 없습니다."));
        Long parentFolderId = folderToDelete.getParentFolderSeq();
        Long driveChannelSeq = folderToDelete.getDriveChannel().getDriveChannelSeq();

        // 하위 폴더 ID 재귀적으로 수집
        List<Long> allFolderIds = new ArrayList<>();
        collectAllSubFolderIds(folderId, allFolderIds);
        allFolderIds.add(folderId);

        // S3 파일들 수집 및 삭제
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

        // 폴더 삭제 (CASCADE로 해당 폴더의 문서들과 DocumentLine들 자동 삭제)
        Collections.reverse(allFolderIds);
        for (Long folderIdToDelete : allFolderIds) {
            folderRepository.deleteById(folderIdToDelete);
        }
        log.info("폴더 CASCADE 삭제 완료: {} 개 폴더", allFolderIds.size());
        
        // 삭제 후 부모 폴더의 순서를 연속적으로 재정렬
        reorderFoldersSequentially(parentFolderId, driveChannelSeq);
    }

    private void collectAllSubFolderIds(Long parentFolderId, List<Long> allFolderIds) {
        Set<Long> visitedFolders = new HashSet<>();
        collectAllSubFolderIdsRecursive(parentFolderId, allFolderIds, visitedFolders);
    }
    
    // 하위 폴더 ID 재귀적으로 수집 (방문 추적)
    private void collectAllSubFolderIdsRecursive(Long parentFolderId, List<Long> allFolderIds, Set<Long> visitedFolders) {
        // 이미 방문한 폴더라면 스킵
        if (visitedFolders.contains(parentFolderId)) {
            return;
        }
        
        // 현재 폴더를 방문한 것으로 표시
        visitedFolders.add(parentFolderId);
        
        List<Folder> subFolders = folderRepository.findByParentFolderSeq(parentFolderId);
        for (Folder subFolder : subFolders) {
            allFolderIds.add(subFolder.getFolderSeq());
            collectAllSubFolderIdsRecursive(subFolder.getFolderSeq(), allFolderIds, visitedFolders);
        }
    }

    // 순환 참조 검증: 이동할 폴더가 현재 폴더의 하위 폴더인지 확인
    private boolean isCircularReference(Long folderId, Long newParentId) {
        if (folderId.equals(newParentId)) {
            return true; // 자기 자신으로 이동하는 경우
        }
        
        // 방문한 폴더를 추적하는 Set
        Set<Long> visitedFolders = new HashSet<>();
        return isCircularReferenceRecursive(folderId, newParentId, visitedFolders);
    }
    
    // 재귀적으로 순환 참조 검증 (방문한 폴더 추적)
    private boolean isCircularReferenceRecursive(Long folderId, Long newParentId, Set<Long> visitedFolders) {
        // 이미 방문한 폴더라면 순환 참조가 아님 (이미 체크했으므로)
        if (visitedFolders.contains(folderId)) {
            return false;
        }
        
        // 현재 폴더를 방문한 것으로 표시
        visitedFolders.add(folderId);
        
        // 재귀적으로 하위 폴더들을 확인
        List<Folder> subFolders = folderRepository.findByParentFolderSeq(folderId);
        for (Folder subFolder : subFolders) {
            if (subFolder.getFolderSeq().equals(newParentId)) {
                return true; // 직접 하위 폴더인 경우
            }
            if (isCircularReferenceRecursive(subFolder.getFolderSeq(), newParentId, visitedFolders)) {
                return true; // 간접 하위 폴더인 경우
            }
        }
        
        return false;
    }

}
