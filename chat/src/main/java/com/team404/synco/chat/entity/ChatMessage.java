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

    @Column(nullable = false)
    private long chatMessageParentSeq;

    @Column(nullable = false)
    @Builder.Default
    private String ynDel = YnColumn.IS_FALSE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatting_channel_member_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private ChatChannelMember chatChannelMember;

    @OneToOne(mappedBy = "chatMessage", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private ChatVote chatVote;
}
