package com.team404.synco.drive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoveItemReqDto {
    private Long itemId;
    private String itemType; // 'folder' or 'document'
    private Long newParentSeq;
}
