package com.team404.synco.member.repository;

import com.team404.synco.common.constant.SocialType;
import com.team404.synco.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmailAndSocialType(String email, SocialType socialType);

    boolean existsByMemberId(String id);

    boolean existsByEmailAndSocialType(String email, SocialType socialType);

    // 로그인: 아이디로 NORMAL 타입 회원 조회
    Optional<Member> findByMemberIdAndSocialType(String memberId, SocialType socialType);

    // 아이디 찾기: 이름과 이메일로 NORMAL 타입 회원 조회
    Optional<Member> findByNameAndEmailAndSocialType(String name, String email, SocialType socialType);

    // 비밀번호 찾기: 아이디와 이메일로 NORMAL 타입 회원 조회
    Optional<Member> findByMemberIdAndEmailAndSocialType(String memberId, String email, SocialType socialType);
}
