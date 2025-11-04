package com.team404.synco.search.index.chat;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

/**
 * ChatMessage 검색용 Elasticsearch Document
 * 인덱스: chat-search-index
 */
@Document(indexName = "chat-search-index")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDocument {
    
    @Id
    private String id;  // "chat_{chatMessageSeq}"
    
    @Field(type = FieldType.Long)
    private Long chatMessageSeq;
    
    @Field(type = FieldType.Text, analyzer = "nori")
    private String content;  // chatMessageText
    
    @Field(type = FieldType.Long)
    private Long workspaceSeq;  // 필수 필터링 (현재 워크스페이스)
    
    @Field(type = FieldType.Long)
    private Long channelSeq;  // 채널 식별자
    
    @Field(type = FieldType.Keyword)
    private String channelName;  // subtitle용 ("채널: 프로젝트팀")
    
    @Field(type = FieldType.Long)
    private Long memberSeq;  // 작성자
    
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Field(type = FieldType.Keyword)
    @Builder.Default
    private String type = "message";  // 프론트에서 구분용
    
}

