package com.team404.synco.workspace.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamDashBoardResDto {
    private Long memberCount;
    // ToDo : 추후 차례대로 Dto에 추가 예정
    public static TeamDashBoardResDto of(){
        return TeamDashBoardResDto.builder()
                .build();
    }
}
