package com.team404.synco.drive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class ToggleReqDto {
    private Long driveChannelSeq;
    private Long documentSeq;
}
