package com.team404.synco.drive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * YJS 업데이트 DTO
 * 클라이언트가 YJS 업데이트를 전송할 때 사용
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YjsUpdateDto {
    /**
     * YJS 바이너리 업데이트 (Base64 인코딩된 문자열)
     */
    private String update;
    
    /**
     * 전체 문서의 텍스트 버전 (Redis 캐싱 및 DB 동기화용)
     * 옵션: 클라이언트가 제공하지 않을 수 있음
     */
    private String textContent;
}

