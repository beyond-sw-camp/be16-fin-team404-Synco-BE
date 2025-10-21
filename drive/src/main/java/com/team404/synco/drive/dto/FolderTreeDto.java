package com.team404.synco.drive.dto;

import com.team404.synco.drive.entity.Folder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 폴더 계층 구조를 위한 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderTreeDto {
    
    private Long folderSeq;
    private String folderName;
    private Long parentFolderSeq;
    private Long orders;
    private Integer depth; // 계층 깊이 (0부터 시작)
    private String path; // 전체 경로 (예: "루트/폴더1/폴더2")
    
    @Builder.Default
    private List<FolderTreeDto> children = new ArrayList<>(); // 하위 폴더들
    
    /**
     * Folder 엔티티를 FolderTreeDto로 변환
     */
    public static FolderTreeDto fromEntity(Folder folder) {
        return FolderTreeDto.builder()
                .folderSeq(folder.getFolderSeq())
                .folderName(folder.getFolderName())
                .parentFolderSeq(folder.getParentFolderSeq())
                .orders(folder.getOrders())
                .build();
    }
    

    // 계층 깊이와 경로를 설정
    public void setDepthAndPath(Integer depth, String path) {
        this.depth = depth;
        this.path = path;
    }
    
    /**
     * 하위 폴더 추가
     */
    public void addChild(FolderTreeDto child) {
        this.children.add(child);
    }
}
