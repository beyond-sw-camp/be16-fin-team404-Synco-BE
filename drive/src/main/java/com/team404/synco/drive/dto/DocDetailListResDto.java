package com.team404.synco.drive.dto;

import com.team404.synco.drive.entity.Type;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocDetailListResDto {
    private Long id;
    private String parentId; // front 부모 종속 id
    private String content;
    private String feId; // front 종속 id
    private Type type;
    private Integer level;
    private Integer indent;
}

