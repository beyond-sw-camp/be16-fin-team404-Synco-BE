package com.team404.synco.chat.entity;

import com.team404.synco.common.constant.Authority;
import jakarta.persistence.*;
import lombok.*;

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
    private Long memberSeq;

    @Column(nullable = false)
    @Builder.Default
    private Authority authority = Authority.SUPER;

    private Long lastReadMessageSeq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatting_channel_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private ChatChannel chatChannel;
}
