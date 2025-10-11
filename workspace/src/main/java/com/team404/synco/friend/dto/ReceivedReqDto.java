package com.team404.synco.friend.dto;

import com.team404.synco.friend.entity.Friend;
import com.team404.synco.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceivedReqDto {
    private Long friendSeq;
    private Long requesterSeq;
    private String requesterId;
    private String requesterName;

    public static ReceivedReqDto fromEntity(Friend friend) {
        Member requester = friend.getMember();
        return ReceivedReqDto.builder()
                .friendSeq(friend.getFriendSeq())
                .requesterSeq(requester.getMemberSeq())
                .requesterId(requester.getMemberId())
                .requesterName(requester.getName())
                .build();
    }
}
