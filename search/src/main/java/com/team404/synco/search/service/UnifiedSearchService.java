package com.team404.synco.search.service;

import com.team404.synco.search.dto.response.SearchResultResDto;
import com.team404.synco.search.dto.request.SearchReqDto;
import com.team404.synco.search.dto.response.UnifiedSearchResDto;
import com.team404.synco.search.index.chat.ChatMessageDocument;
import com.team404.synco.search.index.chat.ChatSearchRepository;
import com.team404.synco.search.index.drive.DriveDocument;
import com.team404.synco.search.index.drive.DriveSearchRepository;
import com.team404.synco.search.index.meeting.MeetingSummaryDocument;
import com.team404.synco.search.index.meeting.MeetingSearchRepository;
import com.team404.synco.search.index.task.TaskDocument;
import com.team404.synco.search.index.task.TaskSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UnifiedSearchService {

    private final TaskSearchRepository taskSearchRepository;
    private final DriveSearchRepository driveSearchRepository;
    private final ChatSearchRepository chatSearchRepository;
    private final MeetingSearchRepository meetingSearchRepository;

    /**
     * 통합 검색 실행
     * 여러 인덱스를 동시에 조회하고 결과를 통합하여 반환
     */
    public UnifiedSearchResDto search(Long workspaceSeq, SearchReqDto searchReqDto) {
        String keyword = searchReqDto.getQuery();
        List<String> types = searchReqDto.getTypes();

        // types가 비어있으면 모든 타입 검색
        if (types == null || types.isEmpty()) {
            types = Arrays.asList("task", "file", "message", "meeting");
        }

        log.info("🔍 검색 실행: keyword={}, types={}, workspaceSeq={}", 
                keyword, types, workspaceSeq);

        // 각 타입별로 검색 실행 (여러 인덱스 동시 조회)
        List<SearchResultResDto> allResults = new ArrayList<>();
        Map<String, Long> facets = new HashMap<>();

        for (String type : types) {
            List<SearchResultResDto> results = searchByType(type, keyword, workspaceSeq);
            allResults.addAll(results);
            facets.put(type, (long) results.size());
        }

        // 페이징 처리
        int page = searchReqDto.getPage();
        int size = searchReqDto.getSize();
        int from = page * size;
        int to = Math.min(from + size, allResults.size());
        
        List<SearchResultResDto> pagedResults = from < allResults.size() 
                ? allResults.subList(from, to) 
                : Collections.emptyList();

        return UnifiedSearchResDto.builder()
                .results(pagedResults)
                .total((long) allResults.size())
                .facets(facets)
                .build();
    }

    /**
     * 타입별 검색 실행
     */
    private List<SearchResultResDto> searchByType(String type, String keyword, Long workspaceSeq) {
        return switch (type) {
            case "task" -> searchTasks(keyword, workspaceSeq);
            case "file" -> searchDrives(keyword, workspaceSeq);
            case "message" -> searchChats(keyword, workspaceSeq);
            case "meeting" -> searchMeetings(keyword, workspaceSeq);
            default -> Collections.emptyList();
        };
    }

    /**
     * Task 검색
     */
    private List<SearchResultResDto> searchTasks(String keyword, Long workspaceSeq) {
        Page<TaskDocument> page;

        page = taskSearchRepository.searchTitleOrContentByWorkspace(
                workspaceSeq, keyword, Pageable.unpaged());

        return page.getContent().stream()
                .map(SearchResultResDto::fromTaskDocument)
                .collect(Collectors.toList());
    }

    /**
     * Drive 검색
     */
    private List<SearchResultResDto> searchDrives(String keyword, Long workspaceSeq) {
        Page<DriveDocument> page = driveSearchRepository
                .searchTitleByWorkspace(workspaceSeq, keyword, Pageable.unpaged()); // 변경

        return page.getContent().stream()
                .map(SearchResultResDto::fromDriveDocument)
                .collect(Collectors.toList());
    }

    /**
     * Chat 검색
     */
    private List<SearchResultResDto> searchChats(String keyword, Long workspaceSeq) {
        Page<ChatMessageDocument> page = chatSearchRepository
                .findByWorkspaceSeqAndContentContaining(workspaceSeq, keyword, Pageable.unpaged());

        return page.getContent().stream()
                .map(SearchResultResDto::fromChatMessageDocument)
                .collect(Collectors.toList());
    }

    /**
     * Meeting 검색
     */
    private List<SearchResultResDto> searchMeetings(String keyword, Long workspaceSeq) {
        Page<MeetingSummaryDocument> page = meetingSearchRepository
                .searchTitleOrContentByWorkspace(workspaceSeq, keyword, Pageable.unpaged());

        return page.getContent().stream()
                .map(SearchResultResDto::fromMeetingSummaryDocument)
                .collect(Collectors.toList());
    }
}

