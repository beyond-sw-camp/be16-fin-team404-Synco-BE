package com.team404.synco.drive.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoveItemReqDto {
    private Long itemId;
    private String itemType; // 'folder' or 'document'
    private Long newParentSeq;
}
