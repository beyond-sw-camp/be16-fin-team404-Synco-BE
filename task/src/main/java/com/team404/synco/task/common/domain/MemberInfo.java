package com.team404.synco.task.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberInfo {
    private long memberSeq;
    private String memberName;
    private String memberProfileImageUrl;
}
