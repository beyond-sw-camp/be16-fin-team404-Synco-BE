package com.team404.synco.workspace.entity;

import com.team404.synco.common.constant.WorkSpaceType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkSpace extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long workSpaceSeq;
    @Column(nullable = false)
    private String workSpaceName;
    private String workSpaceThumbnailImageUrl;
    @Builder.Default
    private WorkSpaceType workSpaceType = WorkSpaceType.INDIVIDUAL;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private Member member;
}
