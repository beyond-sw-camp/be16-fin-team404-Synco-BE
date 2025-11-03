package com.team404.synco.friend.service;

import com.team404.synco.alarm.dto.AlarmResDto;
import com.team404.synco.alarm.service.AlarmService;
import com.team404.synco.common.constant.AlarmType;
import com.team404.synco.common.constant.FriendStatus;
import com.team404.synco.common.constant.WorkSpaceType;
import com.team404.synco.friend.dto.FriendReqDto;
import com.team404.synco.friend.dto.FriendResDto;
import com.team404.synco.friend.dto.ReceivedReqDto;
import com.team404.synco.friend.entity.Friend;
import com.team404.synco.friend.repository.FriendRepository;
import com.team404.synco.member.entity.Member;
import com.team404.synco.member.repository.MemberRepository;
import com.team404.synco.workspace.entity.WorkSpace;
import com.team404.synco.workspace.repository.WorkSpaceRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class FriendService {

    private final FriendRepository friendRepository;
    private final MemberRepository memberRepository;
    private final WorkSpaceRepository workSpaceRepository;
    private final AlarmService alarmService;

    // 1. 친구 요청 보내기
    public void requestFriend(Long memberSeq, FriendReqDto friendReqDto) {
        Member requester = memberRepository.findById(memberSeq)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        Member receiver = memberRepository.findByMemberId(friendReqDto.getFriendMemberId())
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 회원을 찾을 수 없습니다."));

        WorkSpace workSpace = workSpaceRepository.findByMemberAndWorkSpaceType(receiver, WorkSpaceType.INDIVIDUAL)
                .orElseThrow(() -> new EntityNotFoundException("해당 개인 워크스페이스가 존재하지 않습니다."));

        if (requester.equals(receiver)) {
            throw new IllegalArgumentException("자신에게는 친구 요청을 보낼 수 없습니다.");
        }

        if (friendRepository.existsFriendRelation(requester, receiver, FriendStatus.APPROVE)) {
            throw new IllegalArgumentException("이미 친구인 회원입니다.");
        }

        if (friendRepository.existsByMemberAndFriendMemberAndFriendStatus(requester, receiver, FriendStatus.PENDING)) {
            throw new IllegalArgumentException("이미 친구 요청을 보낸 상태입니다.");
        }

        if (friendRepository.existsByMemberAndFriendMemberAndFriendStatus(receiver, requester, FriendStatus.PENDING)) {
            throw new IllegalArgumentException("상대방이 이미 회원님에게 친구 요청을 보냈습니다. 받은 요청 목록을 확인해주세요.");
        }

        Friend newRequest = Friend.builder()
                .member(requester)
                .friendMember(receiver)
                .friendStatus(FriendStatus.PENDING)
                .build();

        Friend friend = friendRepository.save(newRequest);
        sendAlarm(receiver.getMemberSeq(), "[친구 요청] " + requester.getName() + "님이 친구 요청을 보냈습니다.", workSpace.getWorkSpaceSeq(), friend.getFriendSeq());
    }

    // 2. 친구 요청 수락하기
    public void acceptFriendRequest(Long friendSeq, Long memberSeq) {
        Friend pendingRequest = friendRepository.findById(friendSeq)
                .orElseThrow(() -> new IllegalArgumentException("친구 요청을 찾을 수 없습니다."));
        Member member = memberRepository.findById(memberSeq)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));
        WorkSpace workSpace = workSpaceRepository.findByMemberAndWorkSpaceType(member, WorkSpaceType.INDIVIDUAL)
                .orElseThrow(() -> new EntityNotFoundException("해당 개인 워크스페이스가 존재하지 않습니다."));

        if (!pendingRequest.getFriendMember().getMemberSeq().equals(memberSeq)) {
            throw new IllegalArgumentException("권한이 없는 요청입니다.");
        }

        if (pendingRequest.getFriendStatus() == FriendStatus.APPROVE) {
            throw new IllegalArgumentException("이미 수락한 친구 요청입니다.");
        }

        if (pendingRequest.getFriendStatus() != FriendStatus.PENDING) {
            throw new IllegalArgumentException("대기 중인 요청만 수락할 수 있습니다.");
        }

        if (friendRepository.existsFriendRelation(
                pendingRequest.getMember(), 
                pendingRequest.getFriendMember(), 
                FriendStatus.APPROVE)) {
            throw new IllegalArgumentException("이미 친구 관계입니다.");
        }

        pendingRequest.setFriendStatus(FriendStatus.APPROVE);
        friendRepository.save(pendingRequest);

        Friend acceptedRelationship = Friend.builder()
                .member(pendingRequest.getFriendMember())
                .friendMember(pendingRequest.getMember())
                .friendStatus(FriendStatus.APPROVE)
                .build();

        friendRepository.save(acceptedRelationship);
        // 알림을 보내기 위한 멤버 조회
        sendAlarm(pendingRequest.getMember().getMemberSeq(), "[친구요청 수락] " + member.getName() + "님이 친구 요청을 수락했습니다.", workSpace.getWorkSpaceSeq(), pendingRequest.getFriendSeq());
    }

    // 3. 친구 요청 거절하기
    public void rejectFriendRequest(Long friendSeq, Long memberSeq) {
        Friend pendingRequest = friendRepository.findById(friendSeq)
                .orElseThrow(() -> new IllegalArgumentException("친구 요청을 찾을 수 없습니다."));

        // 요청 받은 사람이 맞는지 확인
        if (!pendingRequest.getFriendMember().getMemberSeq().equals(memberSeq)) {
            throw new IllegalArgumentException("권한이 없는 요청입니다.");
        }

        // PENDING 상태만 거절 가능
        if (pendingRequest.getFriendStatus() != FriendStatus.PENDING) {
            throw new IllegalArgumentException("대기 중인 요청만 거절할 수 있습니다.");
        }

        // 요청 삭제
        friendRepository.delete(pendingRequest);
    }

    // 4. 친구 목록 조회 및 검색 (ACCEPTED)
    @Transactional(readOnly = true)
    public Page<FriendResDto> getFriendList(Long memberSeq, Pageable pageable, String keyword) {
        Member member = memberRepository.findById(memberSeq)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        Page<Friend> friendList;
        
        if (keyword != null && !keyword.isBlank()) {
            Specification<Friend> spec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                
                predicates.add(cb.equal(root.get("member"), member));
                predicates.add(cb.equal(root.get("friendStatus"), FriendStatus.APPROVE));
                
                String keywordPattern = "%" + keyword + "%";
                Predicate idLike = cb.like(root.get("friendMember").get("memberId"), keywordPattern);
                Predicate nameLike = cb.like(root.get("friendMember").get("name"), keywordPattern);
                predicates.add(cb.or(idLike, nameLike));
                
                return cb.and(predicates.toArray(new Predicate[0]));
            };
            friendList = friendRepository.findAll(spec, pageable);
        } else {
            friendList = friendRepository.findAllByMemberAndFriendStatus(member, FriendStatus.APPROVE, pageable);
        }

        return friendList.map(FriendResDto::fromEntity);
    }

    // 5. 보낸 요청 목록 조회 (PENDING)
    @Transactional(readOnly = true)
    public Page<FriendResDto> getSentRequestList(Long memberSeq, Pageable pageable) {
        Member member = memberRepository.findById(memberSeq)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        Page<Friend> sentRequestList = friendRepository.findAllByMemberAndFriendStatus(member, FriendStatus.PENDING, pageable);

        return sentRequestList.map(FriendResDto::fromEntity);
    }

    // 6. 받은 요청 목록 조회 (PENDING)
    @Transactional(readOnly = true)
    public Page<ReceivedReqDto> getReceivedRequestList(Long memberSeq, Pageable pageable) {
        Member member = memberRepository.findById(memberSeq)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        Page<Friend> receivedRequestList = friendRepository.findAllByFriendMemberAndFriendStatus(member, FriendStatus.PENDING, pageable);

        return receivedRequestList.map(ReceivedReqDto::fromEntity);
    }

    // 7. 보낸 친구 요청 취소하기
    public void cancelFriendRequest(Long friendSeq, Long memberSeq) {
        Friend sentRequest = friendRepository.findById(friendSeq)
                .orElseThrow(() -> new IllegalArgumentException("친구 요청을 찾을 수 없습니다."));

        // 요청 보낸 사람이 맞는지 확인
        if (!sentRequest.getMember().getMemberSeq().equals(memberSeq)) {
            throw new IllegalArgumentException("권한이 없는 요청입니다.");
        }

        // PENDING 상태만 취소 가능
        if (sentRequest.getFriendStatus() != FriendStatus.PENDING) {
            throw new IllegalArgumentException("대기 중인 요청만 취소할 수 있습니다.");
        }

        // 요청 삭제
        friendRepository.delete(sentRequest);
    }

    // 8. 친구 삭제 (친구 끊기)
    public void deleteFriend(Long friendMemberSeq, Long memberSeq) {
        Member me = memberRepository.findById(memberSeq)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        Member friend = memberRepository.findById(friendMemberSeq)
                .orElseThrow(() -> new IllegalArgumentException("친구를 찾을 수 없습니다."));

        // 친구 관계인지 확인
        if (!friendRepository.existsFriendRelation(me, friend, FriendStatus.APPROVE)) {
            throw new IllegalArgumentException("친구 관계가 아닙니다.");
        }

        // 양방향 관계 조회 및 삭제
        // 1. 내가 member인 관계 삭제 (me -> friend)
        Friend myRelation = friendRepository.findByMemberAndFriendMemberAndFriendStatus(me, friend, FriendStatus.APPROVE)
                .orElseThrow(() -> new IllegalArgumentException("친구 관계를 찾을 수 없습니다."));
        friendRepository.delete(myRelation);

        // 2. 상대방이 member인 관계 삭제 (friend -> me)
        Friend friendRelation = friendRepository.findByMemberAndFriendMemberAndFriendStatus(friend, me, FriendStatus.APPROVE)
                .orElseThrow(() -> new IllegalArgumentException("친구 관계를 찾을 수 없습니다."));
        friendRepository.delete(friendRelation);
    }

    // 친구 목록 조회
    public List<String> findMyFriendList(Long memberSeq){
        Member member = memberRepository.findById(memberSeq)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        Page<Friend> friendList = friendRepository.findAllByMemberAndFriendStatus(member, FriendStatus.APPROVE, Pageable.unpaged());
        return friendList.stream()
                .map(friend -> friend.getFriendMember().getMemberId())
                .collect(Collectors.toList());
    }

    // 알림 전송
    private void sendAlarm(Long memberSeq, String message, Long workSpaceSeq, Long friendSeq){
        // 알림 데이터 조립
        AlarmResDto alarmResDto = AlarmResDto.of(String.valueOf(memberSeq), AlarmType.FRIEND, message, workSpaceSeq, friendSeq);
        alarmService.createAlarm(alarmResDto);
    }
}