package com.team404.synco.drive.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentLine extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentLineSeq;
    @Column(length = 4000)
    private String documentContent;
    @Enumerated(EnumType.STRING)
    private Type type = Type.paragraph; // 기본값을 paragraph로 설정

    private Integer level; // front 태그에 level이 필요한 경우 사용
    private Integer indent; // front tap 기능을 위해 추가

    private String prevFeId; // 이전 블록의 feId
    private String feId; // 프론트에서 적용해주는 uuid 형식의 block id


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private Document document;

    public void updateAllInfo(String content, Type type, Integer level, Integer indent) {
        this.documentContent = content;
        this.type = type;
        this.level = level;
        this.indent = indent;
    }

    public void updatePrevFeId(String prevFeId) {
        this.prevFeId = prevFeId;
    }

    public void updateDocumentContent(String content) {
        this.documentContent = content;
    }

    public void updateType(Type type) {
        this.type = type;
    }

    public void updateIndent(Integer indent) {
        this.indent = indent;
    }
}
