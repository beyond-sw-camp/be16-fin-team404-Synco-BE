package com.team404.synco.chat.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "chatting_channel")
public class ChatChannel extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chatting_channel_seq")
    private long chatChannelSeq;

    private String chatChannelName;

    @Column(nullable = false)
    private long workSpaceSeq;
}
