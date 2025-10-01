package com.team404.synco.drive.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFolderRequest {
    private String folderName;
    private Long parentFolderId;
    private Long driveChannelSeq;
}
