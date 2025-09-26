package com.team404.synco.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatVote extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long voteSeq;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDateTime aliveDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatting_message_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private ChatMessage chatMessage;

    @OneToMany(mappedBy = "chatVoteDetail")
    private List<ChatVoteDetail> chatVoteDetailList = new ArrayList<>();
}
