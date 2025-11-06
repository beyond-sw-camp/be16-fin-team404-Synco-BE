package com.team404.synco.drive.entity;

import com.team404.synco.common.constant.WorkSpaceType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DriveChannel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long driveChannelSeq;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private WorkSpaceType workSpaceType = WorkSpaceType.INDIVIDUAL;
    @Column(nullable = false)
    private String workspaceName;
    @Column(nullable = false)
    private long workspaceSeq;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @OneToMany(mappedBy = "driveChannel", orphanRemoval = true)
    private List<Folder> FolderList = new ArrayList<>();
    @Builder.Default
    @OneToMany(mappedBy = "driveChannel")
    private List<Document> documentList = new ArrayList<>();
}
