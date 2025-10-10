package com.team404.synco.task.entity;

import com.team404.synco.common.constant.Authority;
import com.team404.synco.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleManagementChannelMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleManagementChannelMemberSeq;
    @Column(nullable = false)
    private long memberSeq;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Authority authority = Authority.SUPER;
    @Column(nullable = false)
    private long workSpaceSeq;
    @Builder.Default
    @OneToMany(mappedBy = "scheduleManagementChannelMember", orphanRemoval = true)
    private List<Board> boardList = new ArrayList<>();
    @Builder.Default
    @OneToMany(mappedBy = "picMemberSeq")
    private List<Task> taskList = new ArrayList<>();
    @Builder.Default
    @OneToMany(mappedBy = "scheduleManagementChannelMember", orphanRemoval = true)
    private List<Comment> commentList = new ArrayList<>();
    public void updateAuthority(Authority authority){
        this.authority = authority;
    }
}
