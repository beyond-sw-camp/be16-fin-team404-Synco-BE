package com.team404.synco.member.service;

import com.team404.synco.common.auth.JwtTokenProvider;
import com.team404.synco.common.constant.SocialType;
import com.team404.synco.common.constant.YnColumn;
import com.team404.synco.common.service.S3Uploader;
import com.team404.synco.common.service.EmailService;
import com.team404.synco.member.dto.*;
import com.team404.synco.member.entity.Member;
import com.team404.synco.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private static final String PROFILE_IMAGE_DIRECTORY = "profile";

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Uploader s3Uploader;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    public Long createMemberWithValidation(CreateMemberDto createMemberDto) {

        if (memberRepository.existsByMemberId(createMemberDto.getId())) {
            throw new IllegalArgumentException("이미 가입된 아이디입니다.");
        }

        if (memberRepository.existsByEmailAndSocialType(createMemberDto.getEmail(), SocialType.NORMAL)) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다. (일반 회원가입)");
        }

        String encodedPassword = passwordEncoder.encode(createMemberDto.getPassword());

        String profileImageUrl = null;
        MultipartFile profileImage = createMemberDto.getProfileImage();
        if (profileImage != null && !profileImage.isEmpty()) {
            profileImageUrl = s3Uploader.upload(profileImage, PROFILE_IMAGE_DIRECTORY);
        }

        Member member = memberRepository.save(createMemberDto.toEntity(encodedPassword, profileImageUrl));
        return member.getMemberSeq();
    }

    public LoginResDto doLogin(LoginReqDto loginReqDto) {
        Member member = memberRepository.findByMemberIdAndSocialType(loginReqDto.getMemberId(), SocialType.NORMAL)
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다."));

        if (YnColumn.IS_TRUE.equals(member.getYnDel())) {
            throw new IllegalArgumentException("이미 탈퇴한 계정입니다.");
        }

        if (!passwordEncoder.matches(loginReqDto.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createAtToken(member);
        String refreshToken = jwtTokenProvider.createRtToken(member);

        return LoginResDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional(readOnly = true)
    public MemberResDto myInfo(Long memberSeq) {
        Member member = memberRepository.findById(memberSeq).orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
        return MemberResDto.fromEntity(member);
    }

    public MemberResDto updateMember(Long memberSeq, MemberUpdateDto memberUpdateDto) {
        Member member = memberRepository.findById(memberSeq)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        member.updateMember(memberUpdateDto);

        MultipartFile profileImage = memberUpdateDto.getProfileImage();
        if (profileImage != null && !profileImage.isEmpty()) {
            if (member.getProfileImageUrl() != null && !member.getProfileImageUrl().isEmpty()) {
                try {
                    s3Uploader.delete(member.getProfileImageUrl());
                } catch (Exception e) {
                    log.warn("기존 프로필 이미지 삭제 실패 (계속 진행): {}", e.getMessage());
                }
            }
            String newProfileImageUrl = s3Uploader.upload(profileImage, PROFILE_IMAGE_DIRECTORY);
            member.updateImageUrl(newProfileImageUrl);
        }
        return MemberResDto.fromEntity(member);
    }

    public void deleteMemberYn(Long memberSeq) {
        Member member = memberRepository.findById(memberSeq).orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
        member.deleteMember();
    }

    public LoginResDto generateNewAt(RefreshTokenDto refreshTokenDto) {
        Member member = jwtTokenProvider.validateRt(refreshTokenDto.getRefreshToken());
        String accessToken = jwtTokenProvider.createAtToken(member);
        return LoginResDto.builder()
                .accessToken(accessToken)
                .build();
    }

    @Transactional(readOnly = true)
    public FindIdResDto findMemberId(FindIdReqDto findIdReqDto) {
        Member member = memberRepository.findByNameAndEmailAndSocialType(
                findIdReqDto.getName(),
                findIdReqDto.getEmail(),
                SocialType.NORMAL
        ).orElseThrow(() -> new IllegalArgumentException("일치하는 회원 정보를 찾을 수 없습니다."));

        if (YnColumn.IS_TRUE.equals(member.getYnDel())) {
            throw new IllegalArgumentException("탈퇴한 회원입니다.");
        }

        return FindIdResDto.builder()
                .memberId(member.getMemberId())
                .build();
    }

    public void findPassword(FindPasswordReqDto findPasswordReqDto) {
        Member member = memberRepository.findByMemberIdAndEmailAndSocialType(
                findPasswordReqDto.getMemberId(),
                findPasswordReqDto.getEmail(),
                SocialType.NORMAL
        ).orElseThrow(() -> new IllegalArgumentException("일치하는 회원 정보를 찾을 수 없습니다."));

        if (YnColumn.IS_TRUE.equals(member.getYnDel())) {
            throw new IllegalArgumentException("탈퇴한 회원입니다.");
        }

        String tempPassword = emailService.createTempPassword();
        log.info("임시 비밀번호 생성 완료. 회원: {}", member.getMemberId());

        String encodedTempPassword = passwordEncoder.encode(tempPassword);
        member.updatePassword(encodedTempPassword);
        log.info("임시 비밀번호 DB 저장 완료. 회원: {}", member.getMemberId());

        emailService.sendTempPassword(member.getEmail(), tempPassword);
        log.info("임시 비밀번호 이메일 발송 완료. 회원: {}, 이메일: {}", member.getMemberId(), member.getEmail());
    }

    public void changePassword(Long memberSeq, ChangePasswordReqDto changePasswordReqDto) {
        Member member = memberRepository.findById(memberSeq)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        if (YnColumn.IS_TRUE.equals(member.getYnDel())) {
            throw new IllegalArgumentException("탈퇴한 회원입니다.");
        }

        if (!passwordEncoder.matches(changePasswordReqDto.getCurrentPassword(), member.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        if (passwordEncoder.matches(changePasswordReqDto.getNewPassword(), member.getPassword())) {
            throw new IllegalArgumentException("새 비밀번호는 현재 비밀번호와 다르게 설정해주세요.");
        }

        String encodedNewPassword = passwordEncoder.encode(changePasswordReqDto.getNewPassword());
        member.updatePassword(encodedNewPassword);
        log.info("비밀번호 변경 완료. 회원: {}", member.getMemberId());
    }

}
