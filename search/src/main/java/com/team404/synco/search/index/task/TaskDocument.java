package com.team404.synco.search.index.task;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Task 검색용 Elasticsearch Document
 * 인덱스: task-search-index
 */
@Document(indexName = "task-search-index")
@Setting(settingPath = "/elasticsearch/nori_custom_analyzer.json")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskDocument {
    
    @Id
    private String id;  // "task_{taskSeq}"
    
    @Field(type = FieldType.Long)
    private Long taskSeq;
    
    @Field(type = FieldType.Text, analyzer = "nori_synco_custom")
    private String title;  // taskTitle
    
    @Field(type = FieldType.Text, analyzer = "nori_synco_custom")
    private String content;  // taskContent
    
    @Field(type = FieldType.Keyword)
    private String taskStatus;  // TaskStatus enum (TODO, IN_PROGRESS, COMPLETED)
    
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    @Field(type = FieldType.Long)
    private Long workspaceSeq;  // 필수 필터링 (현재 워크스페이스)
    
    @Field(type = FieldType.Long)
    private Long memberSeq;  // 담당자
    
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Field(type = FieldType.Keyword)
    @Builder.Default
    private String type = "task";  // 프론트에서 구분용
    
}

