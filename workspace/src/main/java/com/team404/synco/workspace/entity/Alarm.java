package com.team404.synco.workspace.entity;

import com.team404.synco.common.constant.AlarmType;
import com.team404.synco.common.constant.YnColumn;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Alarm extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long alarmSeq;
    private AlarmType alarmType;
    @Column(nullable = false)
    @Builder.Default
    private String ynRead = YnColumn.IS_FALSE;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private Member member;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_space_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private WorkSpace workSpace;
}
