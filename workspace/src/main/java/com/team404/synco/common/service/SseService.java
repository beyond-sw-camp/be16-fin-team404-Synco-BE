package com.team404.synco.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team404.synco.alarm.dto.AlarmResDto;
import com.team404.synco.alarm.entity.Alarm;
import com.team404.synco.alarm.repository.AlarmRepository;
import com.team404.synco.common.registry.SseEmitterRegistry;
import com.team404.synco.member.dto.MemberStatusResDto;
import com.team404.synco.member.entity.Member;
import com.team404.synco.member.repository.MemberRepository;
import com.team404.synco.workspace.entity.WorkSpace;
import com.team404.synco.workspace.repository.WorkSpaceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class SseService implements MessageListener {
    private final MemberRepository memberRepository;
    private final WorkSpaceRepository workSpaceRepository;
    private final AlarmRepository alarmRepository;
    private final SseEmitterRegistry sseEmitterRegistry;
    private final ObjectMapper objectMapper;

    public SseService(
            MemberRepository memberRepository,
            WorkSpaceRepository workSpaceRepository,
            AlarmRepository alarmRepository,
            SseEmitterRegistry sseEmitterRegistry,
            ObjectMapper objectMapper
    ) {
        this.memberRepository = memberRepository;
        this.workSpaceRepository = workSpaceRepository;
        this.alarmRepository = alarmRepository;
        this.sseEmitterRegistry = sseEmitterRegistry;
        this.objectMapper = objectMapper;

        // Heartbeat 스케줄러 (25초 주기)
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::sendHeartbeatToAllEmitters, 25, 25, TimeUnit.SECONDS);
    }

    // SSE 연결 (다중 연결 지원)
    public SseEmitter connect(Long userId) {
        SseEmitter sseEmitter = new SseEmitter(0L); // 무제한
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));

        final String memberId = member.getMemberId();

        // 등록
        sseEmitterRegistry.registerEmitter(memberId, sseEmitter);

        // 개별 emitter 기준으로 정리
        sseEmitter.onCompletion(() -> {
            log.info("[SSE 연결 종료] memberId={}", memberId);
            sseEmitterRegistry.removeEmitter(memberId, sseEmitter);
        });

        sseEmitter.onTimeout(() -> {
            log.warn("[SSE 타임아웃] memberId={}", memberId);
            sseEmitterRegistry.removeEmitter(memberId, sseEmitter);
            sseEmitter.complete();
        });

        sseEmitter.onError((e) -> {
            log.error("[SSE 오류] memberId={}, error={}", memberId, e.getMessage());
            sseEmitterRegistry.removeEmitter(memberId, sseEmitter);
            sseEmitter.completeWithError(e);
        });

        // 즉시 초기 연결 이벤트
        try {
            sseEmitter.send(SseEmitter.event()
                    .name("connect")
                    .data("SSE connected")
                    .reconnectTime(3000L));
            log.info("[SSE 연결 성공] memberId={}", memberId);
        } catch (IOException e) {
            log.error("[SSE 초기 메시지 전송 실패] memberId={}", memberId, e);
            sseEmitterRegistry.removeEmitter(memberId, sseEmitter);
            sseEmitter.completeWithError(e);
        }

        return sseEmitter;
    }

    // 특정 사용자에게 알림 전송 (다중 연결 브로드캐스트)
    public void sendToClient(AlarmResDto alarmResDto) {
        Member member = memberRepository.findById(Long.valueOf(alarmResDto.getReceiverId()))
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));
        WorkSpace workSpace = workSpaceRepository.findById(alarmResDto.getWorkSpaceSeq())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 프로젝트 또는 개인 워크스페이스입니다."));

        // DB 저장 (전송 실패 시에도 기록 유지)
        Alarm alarm = alarmRepository.save(alarmResDto.toEntity(member, workSpace, alarmResDto));

        List<SseEmitter> emitters = sseEmitterRegistry.getEmitters(member.getMemberId());
        if (emitters.isEmpty()) {
            log.info("[SSE] emitter 없음 — DB 저장만 수행 (receiverId={})", member.getMemberId());
            return;
        }

        // 안전한 순회를 위해 복사본 사용
        List<SseEmitter> snapshot = new ArrayList<>(emitters);

        for (SseEmitter emitter : snapshot) {
            try {
                emitter.send(SseEmitter.event()
                        .name("alarm")
                        .data(AlarmResDto.fromEntity(alarm))
                        .id(String.valueOf(alarm.getAlarmSeq()))
                        .reconnectTime(3000L));
                log.info("[SSE] 알림 실시간 전송 성공 (to {})", member.getMemberId());
            } catch (IOException | IllegalStateException e) {
                log.warn("[SSE] 전송 실패 → emitter 제거 ({}): {}", member.getMemberId(), e.getMessage());
                sseEmitterRegistry.removeEmitter(member.getMemberId(), emitter);
            }
        }
    }

    // Heartbeat (ping) 주기적 전송 - 다중 연결 브로드캐스트
    private void sendHeartbeatToAllEmitters() {
        try {
            Map<String, List<SseEmitter>> all = sseEmitterRegistry.getAllEmitters();

            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("[SSE Heartbeat] 💓 전송 시작");
            log.info("[SSE Heartbeat] 📊 등록된 사용자 수: {}", all.size());

            if (all.isEmpty()) {
                log.info("[SSE Heartbeat] ⚠️ 전송 대상 없음");
                return;
            }

            int successCount = 0;
            int failCount = 0;

            for (Map.Entry<String, List<SseEmitter>> entry : all.entrySet()) {
                String memberId = entry.getKey();
                List<SseEmitter> snapshot = new ArrayList<>(entry.getValue());

                for (SseEmitter emitter : snapshot) {
                    try {
                        emitter.send(SseEmitter.event()
                                .name("ping")
                                .data("keep-alive")
                                .reconnectTime(3000L));
                        successCount++;
                    } catch (IOException | IllegalStateException e) {
                        log.warn("[SSE] ❌ heartbeat 실패 → emitter 제거 ({}): {}", memberId, e.getMessage());
                        sseEmitterRegistry.removeEmitter(memberId, emitter);
                        failCount++;
                    }
                }
            }

            log.info("[SSE Heartbeat] 📊 전송 완료 - 성공: {}, 실패: {}", successCount, failCount);
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        } catch (Exception e) {
            log.error("[SSE Heartbeat] ❌ 예외 발생: {}", e.getMessage(), e);
        }
    }

    // 멤버 상태 변경(온라인/자리비움/오프라인) - 다중 연결 브로드캐스트 + 이벤트명 지정
    public void changeMemberStatus(MemberStatusResDto memberStatusResDto) {
        Member member = memberRepository.findById(memberStatusResDto.getMemberSeq())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));

        String payload;
        try {
            payload = objectMapper.writeValueAsString(memberStatusResDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        List<SseEmitter> emitters = sseEmitterRegistry.getEmitters(member.getMemberId());
        if (emitters.isEmpty()) {
            log.info("[SSE] member-status 전송 대상 없음: {}", member.getMemberId());
            return;
        }

        List<SseEmitter> snapshot = new ArrayList<>(emitters);
        for (SseEmitter emitter : snapshot) {
            try {
                emitter.send(SseEmitter.event()
                        .name("member-status")
                        .data(payload)
                        .reconnectTime(3000L));
            } catch (IOException | IllegalStateException e) {
                log.warn("[SSE] member-status 전송 실패 → emitter 제거 ({}): {}", member.getMemberId(), e.getMessage());
                sseEmitterRegistry.removeEmitter(member.getMemberId(), emitter);
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
}