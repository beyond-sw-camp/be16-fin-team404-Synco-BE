package com.team404.synco.drive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CursorUpdateDto {
    private Long userId; // 사용자 ID
    private String userName; // 사용자 이름
    private Integer position; // 커서 위치
    private String selection; // 선택된 텍스트 (선택사항)
}
