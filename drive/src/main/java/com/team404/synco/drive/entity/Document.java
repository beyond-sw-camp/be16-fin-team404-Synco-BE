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
    @Column(nullable = false, length = 1000)
    private String documentUrl;
    @Column(nullable = false)
    private long memberSeq;
    @Column(nullable = true)
    private Long fileSize; // 파일 크기 (바이트 단위)
    @Column(nullable = false)
    @Builder.Default
    private String ynLock = YnColumn.IS_FALSE;;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = true)
    private Folder folder;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drive_channel_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private DriveChannel driveChannel;
    @Builder.Default
    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentLine> documentLineList = new ArrayList<>();

    public void updateFolder(Folder folder) {
        this.folder = folder;
    }

    public void updateLockStatus(String ynLock) {
        this.ynLock = ynLock;
    }
}
