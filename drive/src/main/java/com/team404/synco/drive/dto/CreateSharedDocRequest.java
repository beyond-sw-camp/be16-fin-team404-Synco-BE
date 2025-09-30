package com.team404.synco.drive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSharedDocRequest {
    private String documentName;
    private Long parentFolderId;
    private Long driveChannelSeq;
    private Boolean isLocked;
    private String content;
}
