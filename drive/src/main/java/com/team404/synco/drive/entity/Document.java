package com.team404.synco.drive.entity;

import com.team404.synco.common.constant.DocumentType;
import com.team404.synco.common.constant.YnColumn;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Document extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentSeq;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DocumentType documentType;
    @Column(nullable = false)
    private String documentName;
    @Column(nullable = false)
    private String documentUrl;
    @Column(nullable = false)
    private long memberSeq;
    @Column(nullable = false)
    @Builder.Default
    private String ynLock = YnColumn.IS_FALSE;;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private Folder folder;
    @Builder.Default
    @OneToMany(mappedBy = "document")
    private List<DocumentLine> DocumentLineList = new ArrayList<>();

    public void updateFolder(Folder folder) {
        this.folder = folder;
    }

    public void updateLockStatus(String ynLock) {
        this.ynLock = ynLock;
    }
}
