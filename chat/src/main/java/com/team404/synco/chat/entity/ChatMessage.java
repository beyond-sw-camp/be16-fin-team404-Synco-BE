package com.team404.synco.chat.entity;

import com.team404.synco.common.constant.YnColumn;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "chatting_message")
public class ChatMessage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chatting_message_seq")
    private long chatMessageSeq;

    @Column(name = "chatting_message_text", length = 500)
    private String text;

    @Column(name = "chatting_message_file_urls", columnDefinition = "TEXT")
    private String fileUrl;

    @Column(nullable = false)
    private long parentCommentSeq;

    @Column(nullable = false)
    @Builder.Default
    private String yn_del = YnColumn.IS_FALSE;

    @Column(name = "chatting_channel_member_seq", nullable = false)
    private long chatChannelMemberSeq;
}
