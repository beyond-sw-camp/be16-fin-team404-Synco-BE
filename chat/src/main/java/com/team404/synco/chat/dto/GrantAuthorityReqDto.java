package com.team404.synco.chat.dto;

import lombok.*;

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
