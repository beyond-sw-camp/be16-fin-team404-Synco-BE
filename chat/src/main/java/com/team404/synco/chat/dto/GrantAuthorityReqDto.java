package com.team404.synco.chat.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GrantAuthorityReqDto {
    private Long grantMemberSeq;
    private Long workSpaceSeq;
    private String authority;
}
