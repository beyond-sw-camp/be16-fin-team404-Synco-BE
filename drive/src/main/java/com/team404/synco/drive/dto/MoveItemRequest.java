package com.team404.synco.drive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoveItemRequest {
    private Long itemId;
    private String itemType; // 'folder' or 'document'
    private Long newParentId;
}
