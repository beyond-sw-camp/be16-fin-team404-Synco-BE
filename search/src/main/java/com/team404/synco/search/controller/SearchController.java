package com.team404.synco.search.controller;

import com.team404.synco.search.common.dto.ResponseDto;
import com.team404.synco.search.dto.request.SearchReqDto;
import com.team404.synco.search.dto.response.UnifiedSearchResDto;
import com.team404.synco.search.service.UnifiedSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final UnifiedSearchService unifiedSearchService;

    /**
     * 통합 검색 API
     * 여러 인덱스(task, drive, chat, meeting)를 동시에 검색하여 결과 반환
     */
    @PostMapping("/{workspaceSeq}")
    public ResponseEntity<ResponseDto<UnifiedSearchResDto>> unifiedSearch(
            @PathVariable Long workspaceSeq,
            @RequestBody SearchReqDto searchReqDto) {

        log.info("🔍 통합 검색 요청: workspaceSeq={}, query={}, types={}, page={}, size={}",
                workspaceSeq, searchReqDto.getQuery(), searchReqDto.getTypes(),
                searchReqDto.getPage(), searchReqDto.getSize());

        UnifiedSearchResDto results = unifiedSearchService.search(workspaceSeq, searchReqDto);
        return ResponseEntity.ok(ResponseDto.ok(results, HttpStatus.OK));
    }
}

