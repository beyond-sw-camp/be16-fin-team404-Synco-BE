package com.team404.synco.workspace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WorkSpaceInfoResDto {
    private String workSpaceProfileUrl;
    private List<String> myWorkSpaceList;
}
