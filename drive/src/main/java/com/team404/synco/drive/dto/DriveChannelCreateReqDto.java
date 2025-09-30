package com.team404.synco.drive.dto;

import com.team404.synco.drive.entity.DriveChannel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class DriveChannelCreateReqDto {
    private String driveChannelName;
    private Long workspaceSeq;

    public DriveChannel toEntity(Long workspaceSeq){
        return DriveChannel.builder()
                .driveChannelName(this.driveChannelName)
                .workspaceSeq(workspaceSeq)
                .build();
    }
}
