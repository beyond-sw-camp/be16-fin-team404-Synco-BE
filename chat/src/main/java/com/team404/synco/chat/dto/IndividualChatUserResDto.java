package com.team404.synco.chat.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndividualChatUserResDto {
    private Long memberSeq;
    private String memberName;
    private String memberProfileUrl;
    private String activeStatus; // "ONLINE", "OFFLINE" 등
    private List<Long> workSpaceList; // ✅ 추가
}