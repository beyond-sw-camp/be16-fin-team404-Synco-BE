package com.team404.synco.chat.dto;

import com.team404.synco.chat.entity.ChatChannelMember;
import com.team404.synco.common.constant.Authority;
import lombok.Builder;
import lombok.Getter;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelMemberResDto {
    private Long memberSeq;
    private Authority authority;
    private String memberName;
    private String memberProfileUrl;

    public static ChannelMemberResDto of(ChatChannelMember chatChannelMember, String memberName, String memberProfileUrl){
        return ChannelMemberResDto.builder()
                .memberSeq(chatChannelMember.getMemberSeq())
                .authority(chatChannelMember.getAuthority())
                .memberName(memberName)
                .memberProfileUrl(memberProfileUrl)
                .build();
    }
}
