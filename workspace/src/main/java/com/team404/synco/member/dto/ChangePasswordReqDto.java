package com.team404.synco.member.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordReqDto {

    @NotEmpty(message = "현재 비밀번호를 입력해주세요.")
    private String currentPassword;

    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{11,}$",
            message = "비밀번호는 11자 이상이며, 영문, 숫자, 특수문자를 포함해야 합니다.")
    @NotEmpty(message = "비밀번호를 입력해 주세요.")
    private String newPassword;
}

