package com.team404.synco.task.entity;

import com.team404.synco.task.constant.TaskStatus;
import com.team404.synco.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Task extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskSeq;
    @Column(nullable = false)
    private String taskTitle;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String taskContent;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskStatus taskStatus;
    @Column(nullable = false)
    private LocalDate startDate;
    @Column(nullable = false)
    private LocalDate endDate;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pic_member_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private ScheduleManagementChannelMember picMemberSeq;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Board board;
    @Builder.Default
    @OneToMany(mappedBy = "task", orphanRemoval = true)
    private List<Comment> commentList = new ArrayList<>();
}
