package com.team404.synco.chat.dto.channel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelParticipantResDto {
    private Long memberSeq;
    private String memberName;
    private String profileImageUrl;
    private String authority;
}
