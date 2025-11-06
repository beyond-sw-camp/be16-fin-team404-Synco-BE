package com.team404.synco.virtualmeeting.dto.Feign;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GrantAuthorityReqDto {
    private Long workSpaceSeq;
    private Long grantMemberSeq;
    private Long channelSeq;
    private String authority;
}