package com.team404.synco.chat.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "vote_detail")
public class ChatVoteDetail extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vote_detail_seq")
    private long voteDetailSeq;

    @Column(nullable = false)
    private String title;

    @Column(name = "vote_seq", nullable = false)
    private long voteSeq;

    @Column(name = "chatting_channel_member_seq", nullable = false)
    private long chatChannelMemberSeq;
}
