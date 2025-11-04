package com.team404.synco.search.index.drive;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

/**
 * Drive Document 검색용 Elasticsearch Document
 * 인덱스: drive-search-index
 */
@Document(indexName = "drive-search-index")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DriveDocument {
    
    @Id
    private String id;  // "drive_{documentSeq}"
    
    @Field(type = FieldType.Long)
    private Long documentSeq;
    
    @Field(type = FieldType.Text, analyzer = "nori")
    private String title;  // documentName
    
    @Field(type = FieldType.Keyword)
    private String documentType;  // DocumentType enum (LOCAL=외부파일, CUSTOM=문서편집기)
    
    @Field(type = FieldType.Long)
    private Long workspaceSeq;  // 필수 필터링 (현재 워크스페이스)
    
    @Field(type = FieldType.Keyword)
    private String folderName;  // subtitle용 (null 가능 - 최상위일 경우)
    
    @Field(type = FieldType.Long)
    private Long memberSeq;  // 업로더
    
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Field(type = FieldType.Keyword)
    @Builder.Default
    private String type = "file";  // 프론트에서 구분용
    
    // 추가 메타데이터
    @Field(type = FieldType.Keyword)
    private String documentUrl;  // 파일 URL (검색 결과 링크용)
    
    @Field(type = FieldType.Long)
    private Long fileSize;  // 파일 크기 (바이트)
    
}

