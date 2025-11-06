package com.team404.synco.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberUpdateDto {
    @NotEmpty(message = "이름을 입력해 주세요.")
    @Size(min = 2, max = 8, message = "이름은 2자 이상 8자 이하로 입력해야 합니다.")
    private String name;
    @NotEmpty(message = "아이디를 입력해 주세요.")
    @Size(min = 6, max = 13, message = "아이디는 6자 이상 13자 이하로 입력해야 합니다.")
    private String id;
    @NotEmpty(message = "이메일을 입력해 주세요.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;
    private String statusMessage;
    private MultipartFile profileImage;
    private Boolean deleteProfileImage; // 프로필 이미지 삭제 여부
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호는 010-0000-0000 형식으로 입력해야 합니다.")
    private String telNo;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
}
