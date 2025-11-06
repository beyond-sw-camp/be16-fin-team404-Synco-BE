package com.team404.synco.member.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberIdReqDto {
    @NotEmpty(message = "아이디를 입력해 주세요.")
    @Size(min = 6, max = 13, message = "아이디는 6자 이상 13자 이하로 입력해야 합니다.")
    private String memberId;
}
