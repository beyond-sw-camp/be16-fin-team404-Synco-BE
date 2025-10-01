package com.team404.synco.workspace.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class WorkSpaceListResDto {
    Long memberSeq;
    String name;
    String profileImageUrl;
    List<Long> workSpaceList;
}
