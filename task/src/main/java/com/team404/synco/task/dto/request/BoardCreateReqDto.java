package com.team404.synco.task.dto.request;

import com.team404.synco.task.entity.Board;
import com.team404.synco.task.entity.ScheduleManagementChannelMember;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoardCreateReqDto {

    @NotEmpty(message = "보드 이름은 필수입니다.")
    private String boardName;
    @NotEmpty
    private String colors;
    @Positive(message = "보드 생성자 번호는 양수여야 합니다.")
    private long scheduleManagementChannelMemberSeq;
    
    public Board toEntity(final ScheduleManagementChannelMember member, final long orders) {
        return Board.builder()
                .boardName(this.boardName)
                .colors(this.colors)
                .orders(orders) 
                .scheduleManagementChannelMember(member)
                .build();
    }
}
