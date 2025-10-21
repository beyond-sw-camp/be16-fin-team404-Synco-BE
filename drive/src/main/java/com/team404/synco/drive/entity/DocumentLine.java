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

    private String prevId;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Type type = Type.paragraph;

    @Column(unique = true)
    private String lineId; // 프론트에서 적용해주는 uuid 형식의 id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private Document document;

    public void updateContent(String content) {
        this.documentContent = content;
    }
    public void updatePrevId(String prevId){
        this.prevId = prevId;
    }
}
