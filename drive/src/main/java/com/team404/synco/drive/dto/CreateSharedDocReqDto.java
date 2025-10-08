package com.team404.synco.drive.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSharedDocReqDto {
    private String documentName;
    private Long parentFolderSeq;
    private Long driveChannelSeq;
    private Boolean isLocked;
}
