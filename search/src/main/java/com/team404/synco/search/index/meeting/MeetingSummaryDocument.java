package com.team404.synco.search.index.meeting;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

/**
 * Meeting RecordingSummary 검색용 Elasticsearch Document
 * 인덱스: meeting-search-index
 */
@Document(indexName = "meeting-search-index")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MeetingSummaryDocument {
    
    @Id
    private String id;  // "meeting_{recordingSummarySeq}"
    
    @Field(type = FieldType.Long)
    private Long recordingSummarySeq;
    
    @Field(type = FieldType.Long)
    private Long recordingSeq;
    
    @Field(type = FieldType.Text, analyzer = "nori")
    private String title;  // Room의 roomName
    
    @Field(type = FieldType.Text, analyzer = "nori")
    private String description;  // Room의 roomDescription
    
    @Field(type = FieldType.Text, analyzer = "nori")
    private String content;  // summary + transcript
    
    @Field(type = FieldType.Long)
    private Long workspaceSeq;  // 필수 필터링 (현재 워크스페이스)
    
    @Field(type = FieldType.Long)
    private Long roomSeq;
    
    @Field(type = FieldType.Long)
    private Long hostId;  // 호스트
    
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startedAt;  // 회의 시작 시간
    
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Field(type = FieldType.Keyword)
    @Builder.Default
    private String type = "meeting";  // 프론트에서 구분용
    
}

