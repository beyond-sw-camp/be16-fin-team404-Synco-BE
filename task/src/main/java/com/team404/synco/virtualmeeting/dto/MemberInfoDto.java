package com.team404.synco.virtualmeeting.dto;

import com.team404.synco.common.constant.ActiveStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberInfoDto {
    private Long memberSeq;
    private String name;
    private String profileImageUrl;
    private ActiveStatus activeStatus;
}
