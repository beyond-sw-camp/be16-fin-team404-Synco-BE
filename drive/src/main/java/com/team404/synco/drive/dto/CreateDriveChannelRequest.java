package com.team404.synco.drive.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDriveChannelRequest {
    private String driveChannelName;
    private Long workspaceSeq;
}
