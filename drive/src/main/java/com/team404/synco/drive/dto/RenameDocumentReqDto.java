package com.team404.synco.drive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RenameDocumentReqDto {
    private Long driveChannelSeq;
    private Long documentSeq;
    private String newDocumentName;
}

