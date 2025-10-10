package com.team404.synco.drive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RenameFolderReqDto {
    private Long driveChannelSeq;
    private Long folderSeq;
    private String newFolderName;
}