package com.team404.synco.friend.entity;

import com.team404.synco.common.constant.FriendStatus;
import com.team404.synco.member.entity.Member;
import com.team404.synco.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Friend extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long friendSeq;
    @Column(nullable = false)
    private long friendMemberSeq;
    @Column(nullable = false)
    @Builder.Default
    private FriendStatus friendStatus = FriendStatus.PENDING;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private Member member;
}
