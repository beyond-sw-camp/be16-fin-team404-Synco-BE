package com.team404.synco.workspace.dto;

import com.team404.synco.common.constant.Authority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class TaskCreateReqDto {
    private Long memberSeq;
    private Authority authority;
}
