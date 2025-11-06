package com.team404.synco.chat.dto.channel;

import com.team404.synco.chat.entity.ChatChannelMember;
import com.team404.synco.common.constant.Authority;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChannelGrantResDto {
    private Long channelSeq;
    private Long workSpaceSeq;
    private String channelName;
    private Long ownerMemberSeq;
    private Long grantMemberSeq;
    private Authority grantAuthority;

    public static ChannelGrantResDto fromEntity(ChatChannelMember superMember, ChatChannelMember grantMember){
        return ChannelGrantResDto.builder()
                .channelSeq(grantMember.getChatChannel().getChatChannelSeq())
                .workSpaceSeq(grantMember.getChatChannel().getWorkSpaceSeq())
                .channelName(grantMember.getChatChannel().getChatChannelName())
                .ownerMemberSeq(superMember.getMemberSeq())
                .grantMemberSeq(grantMember.getMemberSeq())
                .grantAuthority(grantMember.getAuthority())
                .build();
    }
}
