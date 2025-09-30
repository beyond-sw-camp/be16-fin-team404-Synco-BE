package com.team404.synco.workspace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberUpdateDTO {
    private String id;
    private String name;
    private String email;
    private String statusMessage;
    private String profileImageUrl;
    private String telNo;
}
