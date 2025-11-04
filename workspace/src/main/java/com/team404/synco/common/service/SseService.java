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
import com.team404.synco.workspace.service.WorkSpaceService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
public class SseService implements MessageListener {
    private final MemberRepository memberRepository;
    private final FriendRepository friendRepository;
    private final SseEmitterRegistry sseEmitterRegistry;
    private final ObjectMapper objectMapper;

    @Lazy
    @Autowired
    private WorkSpaceService workSpaceService; // 순환참조 문제로 필드 주입 + Lazy

    public SseService(
            MemberRepository memberRepository, FriendRepository friendRepository,
            SseEmitterRegistry sseEmitterRegistry,
            ObjectMapper objectMapper
    ) {
        this.memberRepository = memberRepository;
        this.friendRepository = friendRepository;
        this.sseEmitterRegistry = sseEmitterRegistry;
        this.objectMapper = objectMapper;
    }

    // SSE 연결 (다중 연결 지원)
    public SseEmitter connect(Long userId) {
        SseEmitter sseEmitter = new SseEmitter(14400 * 60 * 1000L);

        // 🔹 1. 콜백 등록 (여기에 onCompletion / onTimeout 넣기)
        sseEmitter.onCompletion(() -> {
            log.info("[SSE] 연결 종료 → emitter 제거");
            sseEmitterRegistry.removeEmitter(getReceiver(userId));
        });
        sseEmitter.onTimeout(() -> {
            log.info("[SSE] 타임아웃 발생 → emitter 제거");
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
        } catch (IOException e) {
            throw new RuntimeException("SSE 연결 중 오류 발생", e);
        }

        return sseEmitter;
    }

    public void unSubscribe(Long userId) {
        sseEmitterRegistry.removeEmitter(getReceiver(userId));
    }

    // 특정 사용자에게 알림 전송 (다중 연결 브로드캐스트)
    public void sendToClient(AlarmResDto alarmResDto) {

        List<SseEmitter> emitters = sseEmitterRegistry.getEmitters(alarmResDto.getReceiverId());
        if (emitters.isEmpty()) {
            log.info("[SSE] emitter 없음 — DB 저장만 수행 (receiverId={})", alarmResDto.getReceiverId());
            return;
        }

        // 안전한 순회를 위해 복사본 사용
        List<SseEmitter> snapshot = new ArrayList<>(emitters);

        for (SseEmitter emitter : snapshot) {
            try {
                emitter.send(SseEmitter.event()
                        .name("alarm")
                        .data(alarmResDto)
                        .id(String.valueOf(alarmResDto.getAlarmSeq()))
                        .reconnectTime(3000L));
            } catch (IOException | IllegalStateException e) {
                throw new RuntimeException("알림 전송 실패");
            }
        }
    }

    // Heartbeat (ping) 주기적 전송 - 다중 연결 브로드캐스트
    @Scheduled(initialDelay = 0, fixedRate = 15000)
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

        // 1. 해당 멤버의 친구 목록 조회
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));
        List<String> friendList = findMyFriendList(member.getMemberSeq());

        // 2. 해당 멤버가 속한 워크스페이스의 멤버 목록 조회 후 memberId 변환
        List<String> workSpaceMemberList = workSpaceService.findMyWorkSpaceList(member.getMemberSeq()).stream()
                .map(WorkSpaceInfoResDto::getWorkSpaceSeq)
                .flatMap(workSpaceSeq -> workSpaceService.findWorkSpaceMemberList(workSpaceSeq).stream())
                .map(WorkSpaceMemberInfoResDto::getMemberSeq)
                .map(seq -> memberRepository.findById(seq)
                        .map(Member::getMemberId)
                        .orElse(null)) // 존재하지 않는 경우 null 반환
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // 3. 모든 관련 사용자 목록 합치기 (중복 제거)
        Set<String> targetMemberList = new HashSet<>();
        targetMemberList.addAll(friendList);
        targetMemberList.addAll(workSpaceMemberList);

        // 4. 각 사용자의 emitter를 찾아서 전송
        for (String targetMemberId : targetMemberList) {
            log.info("target: " + targetMemberId);
            // 자기 자신은 제외 (이미 상태를 알고 있음)
            if (targetMemberId.equals(memberId)) {
                continue;
            }

            List<SseEmitter> emitters = sseEmitterRegistry.getEmitters(targetMemberId);
            List<SseEmitter> snapshot = new ArrayList<>(emitters);

            for (SseEmitter emitter : snapshot) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("member-status")
                            .data(payload)
                            .reconnectTime(3000L));
                } catch (IOException | IllegalStateException e) {
                    log.info("[SSE] member-status 전송 실패: from={}, to={}, error={}",
                            memberId, targetMemberId, e.getMessage());
                }
            }
        }
    }

    // pub/sub으로 들어온 알림 → 실시간 전송
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            AlarmResDto alarmResDto = objectMapper.readValue(message.getBody(), AlarmResDto.class);
            sendToClient(alarmResDto);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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