package com.team404.synco.workspace.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class PersonalDashBoardResDto {
    private List<WorkSpaceInfoResDto> myWorkSpaceList = new ArrayList<>();
    // ToDo : 추후 차례대로 Dto에 추가 예정
    public static PersonalDashBoardResDto of(List<WorkSpaceInfoResDto> myWorkSpaceList){
        return PersonalDashBoardResDto.builder()
                .myWorkSpaceList(myWorkSpaceList)
                .build();
    }
}
