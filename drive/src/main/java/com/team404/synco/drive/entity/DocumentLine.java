package com.team404.synco.drive.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentLine extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentLineSeq;
    @Column(nullable = false)
    private long documentParentLineSeq;
    @Column(nullable = false)
    private String documentContent;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private Document document;
}
