package com.team404.synco.friend.dto;

import com.team404.synco.common.constant.ActiveStatus;
import com.team404.synco.friend.entity.Friend;
import com.team404.synco.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FriendResDto {
    private Long friendSeq;
    private Long memberSeq;
    private String memberId;
    private String name;
    private String profileImageUrl;
    private ActiveStatus activeStatus;

    public static FriendResDto fromEntity(Friend friend) {
        Member friendMember = friend.getFriendMember();
        return FriendResDto.builder()
                .friendSeq(friend.getFriendSeq())
                .memberSeq(friendMember.getMemberSeq())
                .memberId(friendMember.getMemberId())
                .name(friendMember.getName())
                .profileImageUrl(friendMember.getProfileImageUrl())
                .activeStatus(friendMember.getActiveStatus())
                .build();
    }
}
