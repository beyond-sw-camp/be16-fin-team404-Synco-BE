package com.team404.synco.drive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineLocksResponseDto {
    private Long documentId;
    private List<LineLockDto> locks;
}
