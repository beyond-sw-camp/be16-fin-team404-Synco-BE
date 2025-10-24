package com.team404.synco.virtualmeeting.dto.Room;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatMessageReq {
    private String senderId;
    private String name;
    private String content;
}
