package com.team404.synco.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatChannel extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatChannelSeq;

    @Column(nullable = false)
    private String chatChannelName;

    @Column(nullable = false)
    private long workSpaceSeq;

    @Builder.Default
    @OneToMany(mappedBy = "chatChannel", orphanRemoval = true)
    private List<ChatChannelMember> chatChannelfriendList = new ArrayList<>();
}
