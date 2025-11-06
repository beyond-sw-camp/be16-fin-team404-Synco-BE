package com.team404.synco.friend.repository;


import com.team404.synco.common.constant.FriendStatus;
import com.team404.synco.friend.entity.Friend;
import com.team404.synco.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long>, JpaSpecificationExecutor<Friend> {

    // 이미 친구인지 확인 (양방향, ACCEPTED 상태만)
    @Query("""
        SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END
        FROM Friend f
        WHERE ((f.member = :member1 AND f.friendMember = :member2)
            OR (f.member = :member2 AND f.friendMember = :member1))
        AND f.friendStatus = :status
    """)
    boolean existsFriendRelation(@Param("member1") Member member1, @Param("member2") Member member2, @Param("status") FriendStatus status);

    // 내가 이미 친구 요청을 보냈는지 (A → B, PENDING)
    // 상대방이 나에게 이미 요청을 보냈는지 (B → A, PENDING)
    boolean existsByMemberAndFriendMemberAndFriendStatus(Member member, Member friendMember, FriendStatus friendStatus);

    // 친구 목록 & 보낸 요청 목록 (member와 friendStatus로 조회)
    Page<Friend> findAllByMemberAndFriendStatus(Member member, FriendStatus friendStatus, Pageable pageable);


    // 받은 요청 목록 (상대방이 나에게 보낸 PENDING)
    Page<Friend> findAllByFriendMemberAndFriendStatus(Member member, FriendStatus friendStatus, Pageable pageable);

    // 특정 친구 관계 조회 (친구 삭제 시 사용)
    java.util.Optional<Friend> findByMemberAndFriendMemberAndFriendStatus(Member member, Member friendMember, FriendStatus friendStatus);
}
