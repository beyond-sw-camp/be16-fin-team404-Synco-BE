package com.team404.synco.virtualmeeting.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class KickMemberFromWorkSpaceReqDto {
    private Long workSpaceSeq;
    private Long memberSeq;
}
