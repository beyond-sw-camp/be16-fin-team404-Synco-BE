package com.team404.synco.member.dto;

import com.team404.synco.common.constant.SocialType;
import com.team404.synco.member.entity.Member;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateMemberDto {

    @Size(max = 8, message = "이름은 8자 이하로 입력해야 합니다.")
    private String name;
    private String id;
    private String email;
//    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{11,}$",
//            message = "비밀번호는 11자 이상이며, 영문, 숫자, 특수문자를 포함해야 합니다.")
    private String password;
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호는 010-0000-0000 형식으로 입력해야 합니다.")
    private String telNo;
    private MultipartFile profileImage;

    public Member toEntity(String encodedPassword, String profileImageUrl) {
        return Member.builder()
                .name(this.name)
                .memberId(this.id)
                .email(this.email)
                .password(encodedPassword)
                .telNo(this.telNo)
                .profileImageUrl(profileImageUrl)
                .socialType(SocialType.NORMAL)
                .build();
    }
}
