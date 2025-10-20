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

    @Enumerated(EnumType.STRING)
    private WorkSpaceType workSpaceType;

    @Builder.Default
    @OneToMany(mappedBy = "chatChannel", orphanRemoval = true)
    private List<ChatChannelMember> chatChannelmemberList = new ArrayList<>();

    public void updateChannelName(String chatChannelName){
        this.chatChannelName = chatChannelName;
    }
}
