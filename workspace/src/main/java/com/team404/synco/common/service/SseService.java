package com.team404.synco.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team404.synco.alarm.dto.AlarmResDto;
import com.team404.synco.common.constant.FriendStatus;
import com.team404.synco.common.registry.SseEmitterRegistry;
import com.team404.synco.friend.repository.FriendRepository;
import com.team404.synco.member.dto.MemberStatusResDto;
import com.team404.synco.member.entity.Member;
import com.team404.synco.member.repository.MemberRepository;
import com.team404.synco.workspace.dto.WorkSpaceInfoResDto;
import com.team404.synco.workspace.dto.WorkSpaceMemberInfoResDto;
import com.team404.synco.workspace.service.WorkSpaceRedisService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseService implements MessageListener {
    private final MemberRepository memberRepository;
    private final FriendRepository friendRepository;
    private final SseEmitterRegistry sseEmitterRegistry;
    private final ObjectMapper objectMapper;
    private final WorkSpaceRedisService workSpaceRedisService;

    // SSE 연결 (다중 연결 지원)
    public SseEmitter connect(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId가 null입니다. SSE 연결을 등록할 수 없습니다.");
        }

        SseEmitter sseEmitter = new SseEmitter(14400 * 60 * 1000L);

        // 🔹 1. 콜백 등록 (여기에 onCompletion / onTimeout 넣기)
        sseEmitter.onCompletion(() -> {
            log.info("정상적으로 브라우저에서 연결이 종료되었습니다. onCompletion()");
            sseEmitterRegistry.removeEmitter(getReceiver(userId));
        });
        sseEmitter.onTimeout(() -> {
            log.info("sseEmitter의 연결시간이 초과되었습니다.");
            sseEmitter.complete();
            sseEmitterRegistry.removeEmitter(getReceiver(userId));
        });
        // 1) 레지스트리에 반드시 등록
        sseEmitterRegistry.registerEmitter(getReceiver(userId), sseEmitter);

        // 3) 초기 연결 이벤트
        try {
            sseEmitter.send(SseEmitter.event()
                    .name("connect")
                    .data("SSE connected"));
            log.info("sseEmitter 연결 성공");
        } catch (IOException e) {
            throw new RuntimeException("SSE 연결 중 오류 발생", e);
        }

        return sseEmitter;
    }

    public void unSubscribe(Long userId) {
        log.info("연결 종료");
        sseEmitterRegistry.removeEmitter(getReceiver(userId));
    }

    // 특정 사용자에게 알림 전송 (다중 연결 브로드캐스트)
    public void sendToClient(AlarmResDto alarmResDto) {

        List<SseEmitter> emitters = sseEmitterRegistry.getEmitters(alarmResDto.getReceiverId());
        if (emitters.isEmpty()) {
            return;
        }

        // 안전한 순회를 위해 복사본 사용
        List<SseEmitter> snapshot = new ArrayList<>(emitters);

        for (SseEmitter emitter : snapshot) {
            try {
                emitter.send(SseEmitter.event()
                        .name("alarm")
                        .data(alarmResDto));
            } catch (IOException | IllegalStateException e) {
                throw new RuntimeException("알림 전송 실패");
            }
        }
    }

    // Heartbeat (ping) 주기적 전송 - 다중 연결 브로드캐스트
    @Scheduled(fixedRate = 15000)
    private void sendHeartbeatToAllEmitters() throws Exception {
        Map<String, List<SseEmitter>> all = sseEmitterRegistry.getAllEmitters();

        if (all.isEmpty()) {
            return;
        }

        for (Map.Entry<String, List<SseEmitter>> entry : all.entrySet()) {
            String memberId = entry.getKey();
            List<SseEmitter> snapshot = new ArrayList<>(entry.getValue());
            for (SseEmitter emitter : snapshot) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("ping")
                            .data("keep-alive"));
                } catch (IOException | IllegalStateException e) {
                    log.info("연결 종료, 사용자에 의해 연결이 종료되었습니다.");
                    sseEmitterRegistry.removeEmitter(memberId);
                }
            }
        }
    }


    // 멤버 상태 변경(온라인/자리비움/오프라인) - 다중 연결 브로드캐스트 + 이벤트명 지정
    public void changeMemberStatus(MemberStatusResDto memberStatusResDto) {
        String memberId = memberStatusResDto.getMemberId();

        String payload;
        try {
            payload = objectMapper.writeValueAsString(memberStatusResDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        log.info("[멤버 상태 변경 요청] memberId={}, payload={}", memberId, payload);

        // 1. 해당 멤버의 친구 목록 조회
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));
        List<String> friendList = findMyFriendList(member.getMemberSeq());
        log.info("[1️⃣ 친구 목록 조회 완료] memberSeq={}, 친구 수={}, 친구목록={}",
                member.getMemberSeq(), friendList.size(), friendList);

        // 2. 해당 멤버가 속한 워크스페이스의 멤버 목록 조회 후 memberId 변환 (Redis 기반)
        List<WorkSpaceInfoResDto> myWorkSpaces = workSpaceRedisService.findMyWorkSpaceList(member.getMemberSeq());
        log.info("[2️⃣ Redis 내 내 워크스페이스 목록 조회 완료] 조회된 워크스페이스 수={}, 내용={}",
                myWorkSpaces.size(), myWorkSpaces);

        List<String> workSpaceMemberList = myWorkSpaces.stream()
                .map(WorkSpaceInfoResDto::getWorkSpaceSeq)
                .flatMap(workSpaceSeq -> {
                    List<WorkSpaceMemberInfoResDto> members =
                            workSpaceRedisService.findWorkSpaceMemberList(workSpaceSeq, member.getMemberSeq());
                    log.info("[2-1️⃣ 워크스페이스 멤버 조회] workSpaceSeq={}, 멤버 수={}, 멤버목록={}",
                            workSpaceSeq, members.size(), members);
                    return members.stream();
                })
                .map(WorkSpaceMemberInfoResDto::getMemberSeq)
                .map(seq -> memberRepository.findById(seq)
                        .map(Member::getMemberId)
                        .orElse(null))
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        log.info("[2️⃣ 워크스페이스 전체 멤버 ID 목록] 총 {}명, 목록={}", workSpaceMemberList.size(), workSpaceMemberList);

        // 3. 모든 관련 사용자 목록 합치기 (중복 제거)
        Set<String> targetMemberList = new HashSet<>();
        targetMemberList.addAll(friendList);
        targetMemberList.addAll(workSpaceMemberList);

        log.info("[3️⃣ 최종 브로드캐스트 대상 목록] 총 {}명 (자기 자신 제외 전)", targetMemberList.size());
        log.info("[3️⃣ 대상 목록] {}", targetMemberList);

        // 4. 각 사용자의 emitter를 찾아서 전송
        for (String targetMemberId : targetMemberList) {
            if (targetMemberId.equals(memberId)) {
                log.debug("[⏩ 자기 자신 제외] {}", targetMemberId);
                continue;
            }

            List<SseEmitter> emitters = sseEmitterRegistry.getEmitters(targetMemberId);
            if (emitters.isEmpty()) {
                log.debug("[⚠️ 전송 대상 SSE 연결 없음] memberId={}", targetMemberId);
                continue;
            }

            log.info("[4️⃣ 전송 시작] memberId={}, 연결된 emitter 수={}", targetMemberId, emitters.size());
            List<SseEmitter> snapshot = new ArrayList<>(emitters);

            for (SseEmitter emitter : snapshot) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("member-status")
                            .data(payload));
                    log.info("[✅ 전송 성공] memberId={}, payload={}", targetMemberId, payload);
                } catch (IOException | IllegalStateException e) {
                    log.warn("[❌ 전송 실패] memberId={}, 사유={}", targetMemberId, e.getMessage());
                    sseEmitterRegistry.removeEmitter(targetMemberId);
                }
            }
        }

        log.info("[5️⃣ 상태 변경 브로드캐스트 완료] memberId={}, 전송대상 총 {}명", memberId, targetMemberList.size());
    }


    // pub/sub으로 들어온 알림 → 실시간 전송
    @Override
    public void onMessage(Message message, byte[] pattern) {

    }

    private String getReceiver(Long userId){
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));
        return member.getMemberId();
    }

    // 친구 목록 직접 조회
    private List<String> findMyFriendList(Long memberSeq) {
        Member member = memberRepository.findById(memberSeq)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));
        return friendRepository.findAllByMemberAndFriendStatus(member, FriendStatus.APPROVE, Pageable.unpaged())
                .stream()
                .map(friend -> friend.getFriendMember().getMemberId())
                .toList();
    }
}