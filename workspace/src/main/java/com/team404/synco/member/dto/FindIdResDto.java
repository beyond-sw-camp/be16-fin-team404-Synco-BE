package com.team404.synco.member.dto;

import com.team404.synco.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FindIdResDto {
    private String memberId;
    
    public static FindIdResDto fromEntity(Member member) {
        return FindIdResDto.builder()
                .memberId(member.getMemberId())
                .build();
    }
}

