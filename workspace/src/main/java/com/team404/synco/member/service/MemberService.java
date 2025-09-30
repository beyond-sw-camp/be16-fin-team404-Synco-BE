package com.team404.synco.member.service;

import com.team404.synco.common.constant.SocialType;
import com.team404.synco.common.constant.YnColumn;
import com.team404.synco.common.service.S3Uploader;
import com.team404.synco.member.dto.CreateMemberDto;
import com.team404.synco.member.dto.LoginReqDto;
import com.team404.synco.member.dto.MemberResDto;
import com.team404.synco.member.dto.MemberUpdateDto;
import com.team404.synco.member.entity.Member;
import com.team404.synco.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Transactional      // 영속성 컨텍스트 반영 시 해당 어노테이션 사용
@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Uploader s3Uploader;

    // 회원가입
    public Long save(CreateMemberDto createMemberDto) {

        if (memberRepository.findByMemberId(createMemberDto.getId()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 아이디입니다.");
        }

        if (memberRepository.findByEmailAndSocialType(createMemberDto.getEmail(), SocialType.NORMAL).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다. (일반 회원가입)");
        }

        if (createMemberDto.getPassword().length() < 11) {
            throw new IllegalArgumentException("비밀번호는 11자 이상으로 입력해주세요.");
        }
        String encodedPassword = passwordEncoder.encode(createMemberDto.getPassword());

        String profileImageUrl = null;
        MultipartFile profileImage = createMemberDto.getProfileImage();
        if (profileImage != null && !profileImage.isEmpty()) {
            profileImageUrl = s3Uploader.upload(profileImage, "profile");
        }

        Member member = memberRepository.save(createMemberDto.toEntity(encodedPassword, profileImageUrl)
        );
        return member.getMemberSeq();
    }

    public Member doLogin(LoginReqDto loginReqDto) {
        Member member = memberRepository.findByEmailAndSocialType(loginReqDto.getEmail(), SocialType.NORMAL)
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다."));

        if (YnColumn.IS_TRUE.equals(member.getYnDel())) {
            throw new IllegalArgumentException("이미 탈퇴한 계정입니다.");
        }

        if (!passwordEncoder.matches(loginReqDto.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }
        return member;
    }

    public MemberResDto myInfo(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("member is not found"));
        return MemberResDto.fromEntity(member);
    }

    public MemberResDto updateMember(Long id, MemberUpdateDto memberUpdateDto) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("member is not found"));
        Member updateMember = member.updateMember(memberUpdateDto);
        return MemberResDto.fromEntity(updateMember);
    }

    public void delete(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("member is not found"));
        member.deleteMember("Y");
    }



}
