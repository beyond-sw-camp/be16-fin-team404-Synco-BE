package com.team404.synco.chat.entity;

import com.team404.synco.common.constant.YnColumn;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatMessageSeq;

    @Column(columnDefinition = "TEXT")
    private String chatMessageText;

    @Column(columnDefinition = "TEXT")
    private String chatMessageFileUrls;

    private Long chatMessageParentSeq; // 답장아닌일반메시지=null 처리를 위한 nullable

    @Column(nullable = false)
    @Builder.Default
    private String ynDel = YnColumn.IS_FALSE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_channel_member_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private ChatChannelMember chatChannelMember;

    @OneToOne(mappedBy = "chatMessage", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private ChatVote chatVote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;
}
