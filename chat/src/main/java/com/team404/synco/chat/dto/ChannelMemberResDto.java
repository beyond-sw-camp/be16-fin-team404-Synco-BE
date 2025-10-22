package com.team404.synco.chat.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelMemberResDto {
    private Long memberSeq;
    private String memberName;
    private String profileImageUrl;
}
