package com.team404.synco.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "vote")
public class ChatVote extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vote_seq")
    private long voteSeq;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDateTime aliveDate;

    @Column(name = "chatting_message_seq", nullable = false)
    private long chatMessageSeq;
}
