package com.team404.synco.chat.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatVoteDetail extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vote_detail_seq")
    private Long voteDetailSeq;

    @Column(nullable = false)
    private String title;

    @Column(name = "vote_seq", nullable = false)
    private Long voteSeq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatting_channel_member_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private ChatMember chatMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private ChatVote chatVote;
}
