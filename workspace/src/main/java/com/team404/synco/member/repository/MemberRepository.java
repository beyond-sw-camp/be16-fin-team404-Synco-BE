package com.team404.synco.member.repository;

import com.team404.synco.common.constant.SocialType;
import com.team404.synco.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmailAndSocialType(String email, SocialType socialType);
    Optional<Member> findByMemberId(String id);
    
    boolean existsByMemberId(String id);
    boolean existsByEmailAndSocialType(String email, SocialType socialType);
}
