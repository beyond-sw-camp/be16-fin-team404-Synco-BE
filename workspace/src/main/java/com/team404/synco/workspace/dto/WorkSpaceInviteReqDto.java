package com.team404.synco.workspace.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class WorkSpaceInviteReqDto {
    private Long workSpaceReq;
    private List<Long> memberList;
}
