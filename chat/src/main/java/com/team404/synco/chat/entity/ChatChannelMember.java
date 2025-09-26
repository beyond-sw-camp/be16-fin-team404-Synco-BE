package com.team404.synco.chat.entity;

import com.team404.synco.common.constant.Authority;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMember extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatChannelMemberSeq;

    @Column(nullable = false)
    private long memberSeq;

    @Column(nullable = false)
    @Builder.Default
    private Authority authority = Authority.SUPER;

    private Long lastReadChatMessageSeq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_channel_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private ChatChannel chatChannel;

    @OneToMany(mappedBy = "chatMember")
    private List<ChatMessage> chatMessageList = new ArrayList<>();

    @OneToMany(mappedBy = "chatMember")
    private List<ChatVoteDetail> chatVoteDetailList = new ArrayList<>();
}