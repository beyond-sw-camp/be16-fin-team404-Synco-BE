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
public class DriveChannel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long driveChannelSeq;
    @Column(nullable = false)
    private String driveChannelName;
    @Column(nullable = false)
    private long workspaceSeq;
    @Builder.Default
    @OneToMany(mappedBy = "driveChannel")
    private List<Folder> FolderList = new ArrayList<>();
}
