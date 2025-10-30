package com.team404.synco.workspace.entity;

import com.team404.synco.common.constant.WorkSpaceType;
import com.team404.synco.common.entity.BaseEntity;
import com.team404.synco.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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
    @Enumerated(EnumType.STRING)
    private WorkSpaceType workSpaceType = WorkSpaceType.INDIVIDUAL;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private Member member;

    public void updateWorkSpaceName(String workSpaceName){
        this.workSpaceName = workSpaceName;
    }

    public void updateStartDate(LocalDateTime startDate){this.startDate = startDate; }

    public void updateEndDate(LocalDateTime endDate){this.endDate = endDate; }

    public void updateImageUrl(String imgUrl) {
        this.workSpaceThumbnailImageUrl = imgUrl;
    }

    public void updateSuperMember(Member member){this.member = member;}
}
