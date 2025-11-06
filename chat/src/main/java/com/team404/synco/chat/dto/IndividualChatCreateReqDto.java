package com.team404.synco.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndividualChatCreateReqDto {
    private Long workSpaceSeq;    // 개인 워크스페이스 Seq
    private Long otherMemberSeq;  // 대화 상대 멤버 Seq
}
