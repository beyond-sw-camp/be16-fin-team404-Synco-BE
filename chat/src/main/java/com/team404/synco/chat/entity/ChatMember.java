package com.team404.synco.chat.entity;

import com.team404.synco.common.constant.Authority;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "chatting_channel_member")
public class ChatMember extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chatting_channel_member_seq")
    private long chatChannelMemberSeq;

    @Column(nullable = false)
    private long memberSeq;

    @Column(nullable = false)
    @Builder.Default
    private Authority authority = Authority.SUPER;

    private long lastReadMessageSeq;

    @Column(name = "chatting_channel_seq", nullable = false)
    private long chatChannelSeq;
}
