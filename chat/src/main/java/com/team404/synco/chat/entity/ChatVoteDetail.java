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
    private Long voteDetailSeq;

    @Column(nullable = false)
    private String voteDetailTitle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_channel_member_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private ChatChannelMember chatChannelMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private ChatVote chatVote;
}
