package com.team404.synco.search.index.chat;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.LocalDateTime;

/**
 * ChatMessage 검색용 Elasticsearch Document
 * 인덱스: chat-search-index
 */
@Document(indexName = "chat-search-index")
@Setting(settingPath = "/elasticsearch/nori_custom_analyzer.json")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDocument {

    @Id
    private String id;  // "chat_{chatMessageSeq}"

    @Field(type = FieldType.Long)
    private Long chatMessageSeq;

    @Field(type = FieldType.Text, analyzer = "nori_synco_custom")
    private String content;  // chatMessageText

    @Field(type = FieldType.Long)
    private Long workspaceSeq;  // 워크스페이스

    @Field(type = FieldType.Long)
    private Long channelSeq;  // 채널 ID

    @Field(type = FieldType.Keyword)
    private String channelName;  // subtitle용("채널: 프로젝트명")

    @Field(type = FieldType.Long)
    private Long memberSeq;  // 작성자

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Field(type = FieldType.Keyword)
    @Builder.Default
    private String type = "message";  // 타입 구분용

}

