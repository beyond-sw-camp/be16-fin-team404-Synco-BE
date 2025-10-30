package com.team404.synco.chat.dto;

import com.team404.synco.chat.entity.WorkSpaceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyChatListResDto {
    private Long channelSeq;
    private String channelName;
    private String otherProfileUrl;
    private Long workspaceSeq;
    private WorkSpaceType workSpaceType;
    private Long unreadCount;
    private boolean isGroupChat;
}
