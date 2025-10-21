package com.team404.synco.member.service;

import com.team404.synco.common.auth.JwtTokenProvider;
import com.team404.synco.common.constant.SocialType;
import com.team404.synco.common.constant.YnColumn;
import com.team404.synco.common.service.S3Uploader;
import com.team404.synco.common.service.EmailService;
import com.team404.synco.member.dto.*;
import com.team404.synco.member.entity.Member;
import com.team404.synco.member.repository.MemberRepository;
import com.team404.synco.workspace.service.WorkSpaceService;
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
    private final WorkSpaceService workSpaceService;
    private final GoogleService googleService;
    private final KakaoService kakaoService;
    private final NaverService naverService;

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


        workSpaceService.createIndividualWorkSpace(member.getMemberSeq());
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
                .needMemberId(false)
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

    public LoginResDto generateNewAt(String refreshToken) {
        Member member = jwtTokenProvider.validateRt(refreshToken);
        String accessToken = jwtTokenProvider.createAtToken(member);
        return LoginResDto.builder()
                .accessToken(accessToken)
                .build();
    }

    public void registerMemberId(Long memberSeq, MemberIdReqDto memberIdReqDto) {
        if (memberRepository.existsByMemberId(memberIdReqDto.getMemberId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        Member member = memberRepository.findById(memberSeq)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        if (member.getMemberId() != null) {
            throw new IllegalStateException("이미 아이디가 등록된 회원입니다.");
        }
        member.registerMemberId(memberIdReqDto.getMemberId());
        workSpaceService.createIndividualWorkSpace(member.getMemberSeq());
    }

    public LoginResDto googleLogin(RedirectDto redirectDto) {
        AccessTokenDto accessTokenDto = googleService.getAccessToken(redirectDto.getCode());
        GoogleProfileDto googleProfile = googleService.getGoogleProfile(accessTokenDto.getAccessToken());

        return socialLoginProcess(SocialType.GOOGLE, googleProfile.getSub(), googleProfile.getEmail(), googleProfile.getName(), googleProfile.getPicture());
    }

    public LoginResDto kakaoLogin(RedirectDto redirectDto) {
        AccessTokenDto accessTokenDto = kakaoService.getAccessToken(redirectDto.getCode());
        KakaoProfileDto kakaoProfile = kakaoService.getKakaoProfile(accessTokenDto.getAccessToken());

        return socialLoginProcess(SocialType.KAKAO, kakaoProfile.getId(), kakaoProfile.getKakaoAccount().getEmail(), kakaoProfile.getKakaoAccount().getProfile().getNickname(), kakaoProfile.getKakaoAccount().getProfile().getProfileImageUrl());
    }

    public LoginResDto naverLogin(RedirectDto redirectDto) {
        AccessTokenDto accessTokenDto = naverService.getAccessToken(redirectDto.getCode(), redirectDto.getState());
        NaverProfileDto naverProfile = naverService.getNaverProfile(accessTokenDto.getAccessToken());
        NaverProfileDto.Response response = naverProfile.getResponse();

        return socialLoginProcess(SocialType.NAVER, response.getId(), response.getEmail(), response.getName(), response.getProfileImage());
    }

    private LoginResDto socialLoginProcess(SocialType socialType, String socialId, String email, String name, String profileImageUrl) {
        Member member = memberRepository.findBySocialId(socialId)
                .orElseGet(() -> {
                    Member newMember = Member.builder()
                            .email(email)
                            .name(name)
                            .socialType(socialType)
                            .socialId(socialId)
                            .profileImageUrl(profileImageUrl)
                            .build();
                    return memberRepository.save(newMember);
                });

        if (YnColumn.IS_TRUE.equals(member.getYnDel())) {
            throw new IllegalArgumentException("이미 탈퇴한 계정입니다.");
        }

        boolean needMemberId = member.getMemberId() == null || member.getMemberId().isBlank();

        String accessToken = jwtTokenProvider.createAtToken(member);
        String refreshToken = jwtTokenProvider.createRtToken(member);

        return LoginResDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .needMemberId(needMemberId)
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

        return FindIdResDto.fromEntity(member);
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

        String encodedTempPassword = passwordEncoder.encode(tempPassword);
        member.updatePassword(encodedTempPassword);

        emailService.sendTempPassword(member.getEmail(), tempPassword);
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
    }

    public void logout(Long memberSeq) {
        Member member = memberRepository.findById(memberSeq)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        if (YnColumn.IS_TRUE.equals(member.getYnDel())) {
            throw new IllegalArgumentException("탈퇴한 회원입니다.");
        }

        jwtTokenProvider.deleteRt(memberSeq);
    }

}
