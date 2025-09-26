package com.team404.synco.drive.entity;

import com.team404.synco.common.constant.DocumentType;
import com.team404.synco.common.constant.YnLock;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Document extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentSeq;
    @Column(nullable = false)
    private DocumentType documentType;
    @Column(nullable = false)
    private String documentName;
    @Column(nullable = false)
    private String documentUrl;
    @Column(nullable = false)
    private long memberSeq;
    @Builder.Default
    private YnLock ynLock = YnLock.N;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private Folder folder;
    @OneToMany(mappedBy = "document")
    private List<DocumentLine> DocumentLineList = new ArrayList<>();

}
