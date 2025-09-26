package com.team404.synco.drive.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Folder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long folderSeq;
    private long parentFolderSeq;
    @Column(nullable = false)
    private String folderName;
    @Column(nullable = false)
    private long orders;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drive_channel_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private DriveChannel driveChannel;
    @Builder.Default
    @OneToMany(mappedBy = "folder")
    private List<Document> DocumentList = new ArrayList<>();
}
