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
public class ChatChannelMember extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatChannelMemberSeq;

    @Column(nullable = false)
    private long memberSeq;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Authority authority = Authority.SUPER;

    private long lastReadChatMessageSeq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_channel_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private ChatChannel chatChannel;

    @Builder.Default
    @OneToMany(mappedBy = "chatChannelMember", orphanRemoval = true)
    private List<ChatMessage> chatMessageList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "chatChannelMember", orphanRemoval = true)
    private List<ChatVoteDetail> chatVoteDetailList = new ArrayList<>();

    public void updateAuthority(Authority authority){
        this.authority = authority;
    }
}