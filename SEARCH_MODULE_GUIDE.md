# 검색 모듈 설계 및 구현 가이드

## 📌 프로젝트 개요

- **프로젝트**: Synco (협업 플랫폼)
- **아키텍처**: MSA (Microservices Architecture)
- **기존 서비스**: workspace, task, drive, chat
- **목표**: 통합 검색 기능 구현
- **기술 스택**: Elasticsearch + Kafka + QueryDSL

---

## 🎯 1. 검색 모듈 설계 목표

### 1.1 요구사항

1. **통합 검색 기능**
   - 현재 워크스페이스 내 모든 데이터 검색
   - 검색 대상: 드라이브(문서), 일정관리(태스크), 채팅, 화상회의 요약

2. **인덱스 분리 전략**
   - **현재**: 통합 검색만 구현
   - **향후 확장**: 각 서비스별 독립 검색 API 추가 예정
   - 서비스별 인덱스 분리 (확장성 고려)

3. **기술 스택**
   - **Elasticsearch**: 빠른 풀텍스트 검색
   - **Kafka**: 데이터 동기화 (Event-Driven)
   - **QueryDSL**: 복잡한 쿼리 및 메타데이터 관리

---

## 🏗️ 2. 시스템 아키텍처

### 2.1 전체 구조

```
┌─────────────────────────────────────────────────────────────┐
│                      Frontend (Vue.js)                       │
│              GlobalSearch Component                           │
│              POST /api/search                                │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                   API Gateway                                 │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│              Search Service (새로운 모듈)                    │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  SearchController                                    │   │
│  │  - POST /api/search-service/search                  │   │
│  └─────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  UnifiedSearchService                                │   │
│  │  - Multi-Search로 여러 인덱스 동시 조회             │   │
│  └─────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Index Services                                      │   │
│  │  - TaskIndexService                                 │   │
│  │  - DocumentIndexService                             │   │
│  │  - ChatIndexService                                 │   │
│  │  - MeetingIndexService                              │   │
│  └─────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Event Consumers                                     │   │
│  │  - TaskEventConsumer                                │   │
│  │  - DocumentEventConsumer                            │   │
│  │  - ChatEventConsumer                                │   │
│  └─────────────────────────────────────────────────────┘   │
└───────────┬─────────────────────────┬──────────────────────┘
            │                         │
            ▼                         ▼
    ┌───────────────┐         ┌───────────────┐
    │ Elasticsearch │         │  Kafka        │
    │   Cluster     │         │  Topics       │
    │               │         │               │
    │ - task-search │         │ - task.*      │
    │ - document-*  │         │ - drive.*    │
    │ - chat-*      │         │ - chat.*      │
    │ - meeting-*   │         │ - meeting.*   │
    └───────────────┘         └───────────────┘
            ▲
            │
    ┌───────────────┐
    │   QueryDSL    │
    │  (RDBMS용)    │
    │               │
    │ 검색 히스토리  │
    │ 북마크        │
    │ 메타데이터    │
    └───────────────┘
```

### 2.2 데이터 흐름

```
[각 서비스에서 데이터 변경]
task-service: Task 생성/수정
drive-service: Document 업로드/수정
chat-service: Message 전송
    │
    ▼
[Kafka로 이벤트 발행]
task.task.created
drive.document.created
chat.message.created
    │
    ▼
[Search Service의 Consumer가 이벤트 수신]
TaskEventConsumer
DocumentEventConsumer
ChatEventConsumer
    │
    ▼
[Elasticsearch에 인덱싱]
task-search-index
document-search-index
chat-search-index
    │
    ▼
[사용자가 검색 요청]
    │
    ▼
[UnifiedSearchService가 Multi-Search 실행]
여러 인덱스를 동시에 조회하고 결과 통합
    │
    ▼
[프론트엔드에 결과 반환]
```

---

## 📊 3. Elasticsearch 인덱스 설계

### 3.1 인덱스 분리 구조

**인덱스 분리 이유**: 향후 각 서비스별 독립 검색 API 추가 시 유리

```
Elasticsearch Cluster:
├── task-search-index          # 일정관리(태스크) 검색
│   └── Document Type:
│       - TaskDocument         (태스크)
│       - BoardDocument        (보드)
│       - CommentDocument      (댓글)
│
├── document-search-index      # 드라이브 검색
│   └── Document Type:
│       - DocumentDocument     (문서)
│       - FolderDocument       (폴더)
│
├── chat-search-index          # 채팅 검색
│   └── Document Type:
│       - ChatMessageDocument  (채팅 메시지)
│
└── meeting-search-index       # 화상회의 요약 검색
    └── Document Type:
        - MeetingSummaryDocument (회의 요약)
```

### 3.2 인덱스 Document 구조 예시

#### TaskDocument 구조
```java
@Document(indexName = "task-search-index")
public class TaskDocument {
    @Id
    private String id;  // "task_{taskSeq}"
    
    private Long taskSeq;
    
    @Field(type = FieldType.Text, analyzer = "nori")
    private String title;  // taskTitle
    
    @Field(type = FieldType.Text, analyzer = "nori")
    private String content;  // taskContent
    
    private Long workspaceSeq;  // 필터링용 (현재 워크스페이스)
    private Long boardSeq;
    private String boardName;  // 프론트 subtitle용
    private Long memberSeq;  // 담당자
    private LocalDateTime createdAt;
    private String type = "task";  // 프론트에서 구분용
}
```

#### DocumentDocument 구조
```java
@Document(indexName = "document-search-index")
public class DocumentDocument {
    @Id
    private String id;  // "doc_{documentSeq}"
    
    private Long documentSeq;
    
    @Field(type = FieldType.Text, analyzer = "nori")
    private String title;  // documentName
    
    @Field(type = FieldType.Text, analyzer = "nori")
    private String content;  // documentContent (문서 내용)
    
    private Long workspaceSeq;
    private String folderName;  // 프론트 subtitle용
    private LocalDateTime createdAt;
    private String type = "file";
}
```

### 3.3 인덱스 매핑 설정

```json
{
  "settings": {
    "analysis": {
      "analyzer": {
        "nori": {
          "type": "nori",
          "decompound_mode": "mixed"
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "id": { "type": "keyword" },
      "title": {
        "type": "text",
        "analyzer": "nori",
        "fields": {
          "keyword": { "type": "keyword" }
        }
      },
      "content": {
        "type": "text",
        "analyzer": "nori"
      },
      "workspaceSeq": { "type": "long" },
      "type": { "type": "keyword" },
      "createdAt": { "type": "date" }
    }
  }
}
```

---

## 🔄 4. Kafka 이벤트 설계

### 4.1 Kafka Topic 구조

```
Kafka Topics:
├── task.task.created        (태스크 생성)
├── task.task.updated        (태스크 수정)
├── task.task.deleted        (태스크 삭제)
├── task.board.created       (보드 생성)
├── task.comment.created     (댓글 생성)
│
├── drive.document.created   (문서 생성/업로드)
├── drive.document.updated   (문서 수정)
├── drive.document.deleted   (문서 삭제)
├── drive.folder.created     (폴더 생성)
│
├── chat.message.created     (채팅 메시지 전송)
│
└── meeting.summary.created  (회의 요약 생성)
```

### 4.2 이벤트 메시지 구조

```json
{
  "eventType": "TASK_CREATED",
  "serviceName": "task-service",
  "timestamp": "2025-01-15T10:00:00",
  "data": {
    "taskSeq": 123,
    "taskTitle": "프로젝트 계획 수립",
    "taskContent": "프로젝트 계획을 수립해야 합니다",
    "workspaceSeq": 1,
    "boardSeq": 10,
    "boardName": "백로그",
    "memberSeq": 5,
    "taskStatus": "IN_PROGRESS",
    "startDate": "2025-01-20",
    "endDate": "2025-01-25",
    "createdAt": "2025-01-15T10:00:00"
  }
}
```

### 4.3 Consumer 처리 흐름

```java
@KafkaListener(topics = "task.task.created", groupId = "search-service-group")
public void handleTaskCreated(TaskCreatedEvent event) {
    // 1. 이벤트 검증
    validateEvent(event);
    
    // 2. Document로 변환
    TaskDocument document = TaskDocument.builder()
        .id("task_" + event.getData().getTaskSeq())
        .taskSeq(event.getData().getTaskSeq())
        .title(event.getData().getTaskTitle())
        .content(event.getData().getTaskContent())
        .workspaceSeq(event.getData().getWorkspaceSeq())
        .boardName(event.getData().getBoardName())
        .createdAt(event.getData().getCreatedAt())
        .build();
    
    // 3. Elasticsearch에 인덱싱
    taskIndexService.index(document);
}
```

---

## 🔍 5. 검색 API 설계

### 5.1 API 엔드포인트

```
POST /api/search-service/search
Headers:
  X-Member-Seq: {사용자 ID}
  X-Workspace-Seq: {현재 워크스페이스 ID}

Request Body:
{
  "query": "프로젝트",
  "types": ["task", "file", "message", "meeting"],  // 선택적 (없으면 전체)
  "page": 0,
  "size": 20
}

Response:
{
  "results": [
    {
      "id": "task_123",
      "type": "task",
      "title": "프로젝트 계획 수립",
      "subtitle": "보드: 백로그",
      "icon": "mdi-clipboard-text",
      "iconColor": "blue",
      "date": "2025-01-15T10:00:00",
      "score": 0.95  // 관련도 점수
    },
    {
      "id": "doc_456",
      "type": "file",
      "title": "프로젝트 계획서.docx",
      "subtitle": "문서 폴더",
      "icon": "mdi-file-document",
      "iconColor": "orange",
      "date": "2025-01-14T15:00:00",
      "score": 0.87
    }
  ],
  "total": 50,
  "facets": {
    "task": 20,
    "file": 15,
    "message": 10,
    "meeting": 5
  }
}
```

### 5.2 프론트엔드 요구사항 (분석 결과)

프론트엔드에서 기대하는 검색 결과 형식:
```javascript
{
  id: "task_123",
  type: "task" | "file" | "message" | "user" | "channel",
  title: "검색 결과 제목",
  subtitle: "부제목 (보드명, 폴더명, 채널명 등)",
  icon: "mdi-clipboard-text",
  iconColor: "blue",
  channelId: "chat_8"  // 선택적 (메시지일 때)
}
```

검색 범위:
- `current-workspace` (기본): 현재 워크스페이스 내
- `all`: 전체 검색
- `current-channel`: 현재 채널 내
- `personal`: 개인 워크스페이스
- `project`: 프로젝트 워크스페이스

---

## 💻 6. Elasticsearch 검색 구현 방법

### 6.1 Elasticsearch는 QueryDSL과 별개

**중요**: Elasticsearch는 자체 Query DSL을 사용하며, JPA QueryDSL과는 **완전히 별개**입니다.

```
SQL (관계형 DB)       →   JPA QueryDSL 사용 가능
Elasticsearch Query   →   Elasticsearch 자체 Query DSL 사용
```

### 6.2 Elasticsearch 검색 구현 방법

#### 방법 1: Spring Data Elasticsearch (추천)

```java
@Repository
public interface TaskSearchRepository 
    extends ElasticsearchRepository<TaskDocument, String> {
    
    // 메서드명으로 자동 쿼리 생성 (JPA Repository와 동일한 패턴)
    List<TaskDocument> findByTitleContaining(String keyword);
    
    Page<TaskDocument> findByWorkspaceSeqAndTitleContaining(
        Long workspaceSeq, 
        String keyword, 
        Pageable pageable
    );
    
    // 복잡한 쿼리는 @Query 어노테이션 (JSON 형식)
    @Query("""
        {
          "bool": {
            "must": [
              {"match": {"title": "?0"}},
              {"match": {"content": "?0"}}
            ],
            "filter": [
              {"term": {"workspaceSeq": ?1}}
            ]
          }
        }
        """)
    List<TaskDocument> searchWithCustomQuery(String keyword, Long workspaceSeq);
}
```

#### 방법 2: ElasticsearchClient (Query Builder)

```java
@Service
public class UnifiedSearchService {
    private final ElasticsearchClient esClient;
    
    public List<TaskDocument> search(String keyword, Long workspaceSeq) {
        SearchRequest searchRequest = SearchRequest.of(s -> s
            .index("task-search-index")
            .query(q -> q
                .bool(b -> b
                    .must(m -> m.match(ma -> ma
                        .field("title")
                        .query(keyword)
                        .fuzziness("AUTO")
                    ))
                    .filter(f -> f.term(t -> t
                        .field("workspaceSeq")
                        .value(workspaceSeq)
                    ))
                )
            )
            .highlight(h -> h
                .fields("title", f -> f)
                .fields("content", f -> f)
            )
        );
        
        SearchResponse<TaskDocument> response = esClient.search(
            searchRequest, 
            TaskDocument.class
        );
        
        return response.hits().hits().stream()
            .map(hit -> hit.source())
            .collect(Collectors.toList());
    }
}
```

### 6.3 Multi-Search 구현 (통합 검색)

```java
@Service
@RequiredArgsConstructor
public class UnifiedSearchService {
    private final ElasticsearchClient esClient;
    
    public UnifiedSearchResponse search(SearchRequest request, Long workspaceSeq) {
        // Multi-Search 요청 생성
        List<co.elastic.clients.elasticsearch.core.SearchRequest> searchRequests = 
            new ArrayList<>();
        
        // 타입별 검색 요청 추가
        if (request.getTypes().contains("task")) {
            searchRequests.add(buildTaskSearch(request.getQuery(), workspaceSeq));
        }
        if (request.getTypes().contains("file")) {
            searchRequests.add(buildDocumentSearch(request.getQuery(), workspaceSeq));
        }
        if (request.getTypes().contains("message")) {
            searchRequests.add(buildChatSearch(request.getQuery(), workspaceSeq));
        }
        
        // Multi-Search 실행
        MultiSearchRequest multiRequest = new MultiSearchRequest(searchRequests);
        MultiSearchResponse response = esClient.msearch(multiRequest);
        
        // 결과 통합
        List<SearchResultDto> allResults = aggregateResults(response);
        
        // 관련도 기준 정렬 (ES score)
        allResults.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        
        // 페이징
        int from = request.getPage() * request.getSize();
        int to = Math.min(from + request.getSize(), allResults.size());
        List<SearchResultDto> pagedResults = allResults.subList(from, to);
        
        return UnifiedSearchResponse.builder()
            .results(pagedResults)
            .total((long) allResults.size())
            .build();
    }
    
    private SearchRequest buildTaskSearch(String query, Long workspaceSeq) {
        return SearchRequest.of(s -> s
            .index("task-search-index")
            .query(q -> q
                .bool(b -> b
                    .must(m -> m.match(ma -> ma
                        .field("title")
                        .query(query)
                        .fuzziness("AUTO")
                    ))
                    .filter(f -> f.term(t -> t
                        .field("workspaceSeq")
                        .value(workspaceSeq)
                    ))
                )
            )
        );
    }
}
```

---

## 🔧 7. QueryDSL 사용 범위

### 7.1 QueryDSL 사용 시나리오

| 용도 | 저장소 | QueryDSL 필요? | 이유 |
|------|--------|---------------|------|
| **Elasticsearch 검색** | ES 인덱스 | ❌ 불필요 | Elasticsearch 자체 쿼리 사용 |
| **검색 히스토리 조회** | RDBMS | ⚠️ 선택적 | 간단하면 JpaRepository만으로 가능 |
| **복잡한 통계 조회** | RDBMS | ✅ 필요 | 동적 조건, 그룹핑 등 복잡한 쿼리 |
| **북마크 관리** | RDBMS | ⚠️ 선택적 | 간단하면 JpaRepository만으로 가능 |

### 7.2 QueryDSL 사용 예시

#### 검색 히스토리 엔티티
```java
@Entity
@Table(name = "search_history")
public class SearchHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long memberSeq;
    private String keyword;
    private LocalDateTime searchedAt;
    private Integer resultCount;
    private String searchTypes;  // "task,file,message"
}
```

#### 간단한 조회 (QueryDSL 불필요)
```java
@Repository
public interface SearchHistoryRepository 
    extends JpaRepository<SearchHistory, Long> {
    
    // JpaRepository만으로 충분!
    List<SearchHistory> findByMemberSeqOrderBySearchedAtDesc(Long memberSeq);
}
```

#### 복잡한 조회 (QueryDSL 필요)
```java
@Repository
@RequiredArgsConstructor
public class SearchHistoryQueryRepository {
    private final JPAQueryFactory queryFactory;
    
    // 복잡한 통계 조회
    public List<PopularKeyword> findPopularKeywords(
        Long memberSeq,
        LocalDate startDate,
        LocalDate endDate
    ) {
        return queryFactory
            .select(new QPopularKeyword(
                searchHistory.keyword,
                searchHistory.keyword.count().as("count")
            ))
            .from(searchHistory)
            .where(
                searchHistory.memberSeq.eq(memberSeq)
                    .and(searchHistory.searchedAt.between(
                        startDate.atStartOfDay(),
                        endDate.atTime(LocalTime.MAX)
                    ))
                    .and(searchHistory.resultCount.goe(5))  // 5개 이상 결과인 것만
            )
            .groupBy(searchHistory.keyword)
            .orderBy(searchHistory.keyword.count().desc())
            .limit(10)
            .fetch();
    }
}
```

---

## 🏛️ 8. Search Service 모듈 구조

### 8.1 전체 디렉토리 구조

```
search-service/
├── build.gradle
├── src/main/
│   ├── java/com/team404/synco/search/
│   │   ├── SearchApplication.java
│   │   │
│   │   ├── config/
│   │   │   ├── ElasticsearchConfig.java          # ES 클라이언트 설정
│   │   │   ├── KafkaConsumerConfig.java          # Kafka Consumer 설정
│   │   │   └── QueryDSLConfig.java               # QueryDSL 설정
│   │   │
│   │   ├── index/                                # 인덱스별 관리
│   │   │   ├── task/
│   │   │   │   ├── TaskDocument.java
│   │   │   │   ├── TaskIndexService.java
│   │   │   │   └── TaskSearchRepository.java     # ES Repository
│   │   │   │
│   │   │   ├── document/
│   │   │   │   ├── DocumentDocument.java
│   │   │   │   ├── DocumentIndexService.java
│   │   │   │   └── DocumentSearchRepository.java
│   │   │   │
│   │   │   ├── chat/
│   │   │   │   ├── ChatDocument.java
│   │   │   │   ├── ChatIndexService.java
│   │   │   │   └── ChatSearchRepository.java
│   │   │   │
│   │   │   └── meeting/
│   │   │       ├── MeetingDocument.java
│   │   │       ├── MeetingIndexService.java
│   │   │       └── MeetingSearchRepository.java
│   │   │
│   │   ├── consumer/                             # Kafka 이벤트 처리
│   │   │   ├── TaskEventConsumer.java
│   │   │   ├── DocumentEventConsumer.java
│   │   │   ├── ChatEventConsumer.java
│   │   │   └── MeetingEventConsumer.java
│   │   │
│   │   ├── service/
│   │   │   ├── UnifiedSearchService.java         # 통합 검색 서비스
│   │   │   ├── SearchResultMapper.java           # ES 결과 → DTO 변환
│   │   │   └── SearchStrategyService.java        # 검색 방식 결정
│   │   │
│   │   ├── repository/                           # QueryDSL (검색 메타데이터용)
│   │   │   ├── querydsl/
│   │   │   │   ├── SearchHistoryQueryRepository.java
│   │   │   │   └── SearchBookmarkQueryRepository.java
│   │   │   │
│   │   │   └── jpa/
│   │   │       ├── SearchHistoryRepository.java  # JPA Repository
│   │   │       └── SearchBookmarkRepository.java
│   │   │
│   │   ├── dto/
│   │   │   ├── request/
│   │   │   │   └── SearchRequest.java
│   │   │   ├── response/
│   │   │   │   └── UnifiedSearchResponse.java
│   │   │   └── SearchResultDto.java
│   │   │
│   │   └── controller/
│   │       └── SearchController.java
│   │           └── POST /api/search-service/search
│   │
│   └── resources/
│       ├── application.yml
│       └── application-local.yml
```

### 8.2 주요 클래스 설명

#### UnifiedSearchService
```java
@Service
@RequiredArgsConstructor
public class UnifiedSearchService {
    private final ElasticsearchClient esClient;
    private final TaskSearchRepository taskSearchRepository;
    private final DocumentSearchRepository documentSearchRepository;
    private final ChatSearchRepository chatSearchRepository;
    
    /**
     * 통합 검색 수행
     * - Multi-Search로 여러 인덱스 동시 조회
     * - 결과 통합 및 정렬
     */
    public UnifiedSearchResponse search(
        SearchRequest request,
        Long workspaceSeq,
        Long memberSeq
    ) {
        // Multi-Search 구현
        // ...
    }
}
```

#### TaskEventConsumer
```java
@Component
@RequiredArgsConstructor
public class TaskEventConsumer {
    private final TaskIndexService taskIndexService;
    
    @KafkaListener(topics = "task.task.created", groupId = "search-service-group")
    public void handleTaskCreated(TaskCreatedEvent event) {
        // 이벤트를 Document로 변환하고 인덱싱
        TaskDocument document = convertToDocument(event);
        taskIndexService.index(document);
    }
}
```

---

## 📝 9. 핵심 정리 및 FAQ

### 9.1 Elasticsearch vs QueryDSL

**Q: Elasticsearch 검색에 QueryDSL이 필요한가?**
- **A**: ❌ 불필요합니다. Elasticsearch는 자체 Query DSL을 사용하며, Spring Data Elasticsearch나 ElasticsearchClient로 처리합니다.

**Q: 언제 QueryDSL을 사용하나?**
- **A**: 검색 서비스 자체의 RDBMS 데이터(검색 히스토리, 북마크 등)를 조회할 때입니다. 단순 조회는 JpaRepository만으로도 충분하며, 복잡한 통계/조건 쿼리일 때 QueryDSL이 유용합니다.

### 9.2 인덱스 분리 이유

**Q: 인덱스를 왜 분리하나?**
- **A**: 현재는 통합 검색만 제공하지만, 향후 각 서비스별 독립 검색 API를 추가할 수 있도록 확장성을 고려한 설계입니다. 인덱스를 나눠두면 독립 검색 API 추가가 쉽고, 서비스별 최적화 및 장애 격리에도 유리합니다.

### 9.3 하이브리드 방식

**Q: "하이브리드"는 무엇인가?**
- **A**: Elasticsearch로 검색 결과를 가져오고, 필요 시 QueryDSL로 보조 정보(RDBMS 기반 메타데이터)를 추가하는 방식입니다.

```
Elasticsearch 검색 (주요 검색 결과)
    +
QueryDSL 조회 (검색 히스토리, 통계 등 보조 정보)
```

### 9.4 통합 검색 범위

**Q: 통합 검색 범위는?**
- **A**: 현재는 **현재 워크스페이스(`current-workspace`) 내에서만 검색**합니다. 필요한 경우 프론트엔드 요청에 맞춰 범위를 확장할 수 있습니다.

---

## 🚀 10. 구현 체크리스트

### Phase 1: 기본 구조 설정
- [ ] search-service 모듈 생성
- [ ] Elasticsearch 설정 및 클라이언트 구성
- [ ] Kafka Consumer 설정
- [ ] QueryDSL 설정 (검색 히스토리용)

### Phase 2: 인덱스 및 Document 설계
- [ ] task-search-index Document 설계
- [ ] document-search-index Document 설계
- [ ] chat-search-index Document 설계
- [ ] meeting-search-index Document 설계
- [ ] 인덱스 매핑 설정 (nori analyzer)

### Phase 3: Kafka Consumer 구현
- [ ] TaskEventConsumer 구현
- [ ] DocumentEventConsumer 구현
- [ ] ChatEventConsumer 구현
- [ ] MeetingEventConsumer 구현

### Phase 4: 통합 검색 API 구현
- [ ] UnifiedSearchService 구현
- [ ] Multi-Search 구현
- [ ] 결과 통합 및 정렬 로직
- [ ] 프론트 형식 변환 (SearchResultDto)
- [ ] SearchController 구현

### Phase 5: 각 서비스에서 Kafka 이벤트 발행
- [ ] task-service: Task 생성/수정 시 이벤트 발행
- [ ] drive-service: Document 업로드/수정 시 이벤트 발행
- [ ] chat-service: Message 전송 시 이벤트 발행
- [ ] task-service: Meeting 요약 생성 시 이벤트 발행

### Phase 6: 검색 메타데이터 (선택적)
- [ ] 검색 히스토리 저장 (QueryDSL 사용)
- [ ] 인기 검색어 통계 (QueryDSL 사용)
- [ ] 북마크 기능 (QueryDSL 사용)

---

## 📚 11. 참고 사항

### 11.1 기술 스택 버전

- Spring Boot: 3.5.6
- Java: 17
- Elasticsearch: 8.x (또는 호환 버전)
- Spring Data Elasticsearch: 최신 버전
- Spring Kafka: 최신 버전
- QueryDSL: 최신 버전

### 11.2 설정 예시

#### application.yml
```yaml
spring:
  elasticsearch:
    uris: http://localhost:9200
    username: elastic
    password: password
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: search-service-group
      auto-offset-reset: earliest

spring.data.elasticsearch:
  repositories:
    enabled: true
```

#### build.gradle
```gradle
dependencies {
    // Elasticsearch
    implementation 'org.springframework.boot:spring-boot-starter-data-elasticsearch'
    
    // Kafka
    implementation 'org.springframework.kafka:spring-kafka'
    
    // QueryDSL
    implementation 'com.querydsl:querydsl-jpa:5.0.0:jakarta'
    annotationProcessor 'com.querydsl:querydsl-apt:5.0.0:jakarta'
    
    // 기타
    implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'
}
```

---

## 💡 12. 주의사항 및 베스트 프랙티스

### 12.1 데이터 일관성
- Elasticsearch는 최종 일관성을 보장하며, 짧은 딜레이는 허용됩니다.
- Kafka 이벤트 처리 실패 시 Dead Letter Queue 사용 권장.

### 12.2 성능 고려사항
- Bulk 인덱싱 사용 권장.
- 인덱스별 샤드/레플리카 설정 최적화.
- 검색 결과 페이징 처리 (기본 20개).

### 12.3 보안
- Elasticsearch 접근 권한 설정.
- 민감 정보는 인덱싱하지 않거나 필터링.

### 12.4 모니터링
- 검색 성능 모니터링.
- Kafka Consumer lag 모니터링.
- Elasticsearch 클러스터 상태 모니터링.

---

## 📌 마무리

이 가이드는 다음을 목표로 합니다:
- 현재는 통합 검색만 구현하되, 인덱스는 서비스별로 분리
- Elasticsearch로 빠른 검색 수행
- QueryDSL은 검색 메타데이터 관리용으로 선택적 사용
- 향후 각 서비스별 독립 검색 API 추가 가능한 구조로 설계

---

**문서 작성일**: 2025-01-15  
**프로젝트**: Synco Backend  
**버전**: 1.0

