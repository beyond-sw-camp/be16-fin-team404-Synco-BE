package com.team404.synco.workspace.dto;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class WorkSpaceInfoResDto {
    private String workSpaceProfileUrl;
    private List<String> myWorkSpaceList;
}
