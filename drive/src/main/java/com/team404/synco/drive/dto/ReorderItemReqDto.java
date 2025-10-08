package com.team404.synco.drive.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReorderItemReqDto {
    private Long itemId;
    private String itemType; // 'folder' or 'document'
    private Long newOrder; // 새로운 순서
}
