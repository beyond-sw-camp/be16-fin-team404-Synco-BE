package com.team404.synco.workspace.dto;

import com.team404.synco.workspace.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateMemberDTO {

    private String name;
    private String id;
    private String email;
    private String password;

    public Member toEntity(String encodedPassword) {
        return Member.builder()
                .name(this.name)
                .memberId(this.id)
                .email(this.email)
                .password(encodedPassword)
                .build();
    }
}
