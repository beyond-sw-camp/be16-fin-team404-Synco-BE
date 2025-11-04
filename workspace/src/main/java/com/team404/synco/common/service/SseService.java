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
                    log.info("연결 종료 : {}", e.getMessage());
                    sseEmitterRegistry.removeEmitter(memberId);
                }
            }
        }
    }


    // 멤버 상태 변경(온라인/자리비움/오프라인) - 다중 연결 브로드캐스트 + 이벤트명 지정
    public void changeMemberStatus(MemberStatusResDto memberStatusResDto) {
        String memberId = memberStatusResDto.getMemberId();

        String payload;
        try { payload = objectMapper.writeValueAsString(memberStatusResDto); }
        catch (JsonProcessingException e) { throw new RuntimeException(e); }

        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));
        List<String> friendList = findMyFriendList(member.getMemberSeq());

        // 내 워크스페이스 목록 (Redis DTO / Fallback(Long) 모두 대응)
        List<?> myWorkSpaceList = workSpaceRedisService.findMyWorkSpaceList(member.getMemberSeq());
        List<Long> workSpaceSeqList =
                (!myWorkSpaceList.isEmpty() && myWorkSpaceList.get(0) instanceof WorkSpaceInfoResDto)
                        ? ((List<WorkSpaceInfoResDto>) myWorkSpaceList).stream()
                        .map(WorkSpaceInfoResDto::getWorkSpaceSeq).filter(Objects::nonNull).distinct().toList()
                        : (!myWorkSpaceList.isEmpty() && myWorkSpaceList.get(0) instanceof Long)
                        ? ((List<Long>) myWorkSpaceList).stream().skip(1).filter(Objects::nonNull).distinct().toList()
                        : Collections.emptyList();

        // 각 워크스페이스 멤버 가져오기(REDIS만 사용, SUPER 식별 불필요)
        Set<Long> memberSeqs = new HashSet<>();
        for (Long wsSeq : workSpaceSeqList) {
            List<?> raw = workSpaceRedisService.findWorkSpaceMemberList(wsSeq, null);
            if (!raw.isEmpty() && raw.get(0) instanceof WorkSpaceMemberInfoResDto) {
                ((List<WorkSpaceMemberInfoResDto>) raw).forEach(m -> { if (m.getMemberSeq()!=null) memberSeqs.add(m.getMemberSeq()); });
            } else if (!raw.isEmpty() && raw.get(0) instanceof Long) {
                ((List<Long>) raw).stream().filter(Objects::nonNull).forEach(memberSeqs::add);
            }
        }

        // memberSeq -> memberId
        List<String> workSpaceMemberList = memberSeqs.isEmpty()
                ? Collections.emptyList()
                : memberRepository.findAllById(memberSeqs).stream()
                .map(Member::getMemberId).filter(Objects::nonNull).distinct().toList();

        // 대상 합치기(본인 제외)
        Set<String> targetMemberList = new HashSet<>();
        targetMemberList.addAll(friendList);
        targetMemberList.addAll(workSpaceMemberList);
        targetMemberList.remove(memberId);

        // 브로드캐스트
        for (String targetMemberId : targetMemberList) {
            List<SseEmitter> emitters = sseEmitterRegistry.getEmitters(targetMemberId);
            if (emitters == null || emitters.isEmpty()) continue;
            List<SseEmitter> snapshot = new ArrayList<>(emitters);
            for (SseEmitter emitter : snapshot) {
                try {
                    emitter.send(SseEmitter.event().name("member-status").data(payload));
                } catch (IOException | IllegalStateException e) {
                    log.info("멤버 상태 전송 실패 -> 연결 종료 : {}", e.getMessage());
                    sseEmitterRegistry.removeEmitter(targetMemberId); // broken-pipe 정리
                }
            }
        }
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