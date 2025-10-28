package com.team404.synco.alarm.entity;

import com.team404.synco.common.constant.YnColumn;
import com.team404.synco.common.entity.BaseEntity;
import com.team404.synco.member.entity.Member;
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
    @Column(nullable = false)
    private String alarmType;
    @Column(nullable = false)
    @Builder.Default
    private String ynRead = YnColumn.IS_FALSE;
    private String message;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private Member member;
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "work_space_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
//    private WorkSpace workSpace;
    public void updateReadStatus(){
        this.ynRead = YnColumn.IS_TRUE;
    }
}
