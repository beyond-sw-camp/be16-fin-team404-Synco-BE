package com.team404.synco.drive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFolderReqDto {
    private String folderName;
    private Long parentFolderSeq;
    private Long driveChannelSeq;
}
