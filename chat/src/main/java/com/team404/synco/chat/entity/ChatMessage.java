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

    @Column(name = "chatting_message_text", columnDefinition = "TEXT")
    private String text;

    @Column(name = "chatting_message_file_urls", columnDefinition = "TEXT")
    private String fileUrl;

    @Column(nullable = false)
    private long parentCommentSeq;

    @Column(nullable = false)
    @Builder.Default
    private String delYn = YnColumn.IS_FALSE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatting_channel_member_seq", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private ChatMember chatMember;

    @OneToOne(mappedBy = "chatMessage", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private ChatVote chatVote;
}
