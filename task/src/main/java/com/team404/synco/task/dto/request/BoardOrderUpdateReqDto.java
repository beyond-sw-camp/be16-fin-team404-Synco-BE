package com.team404.synco.task.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoardOrderUpdateReqDto {
    private long boardSeq;
    private long newOrders;
}
