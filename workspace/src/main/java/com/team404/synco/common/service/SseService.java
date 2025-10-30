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

    public SseService(MemberRepository memberRepository, WorkSpaceRepository workSpaceRepository, AlarmRepository alarmRepository, SseEmitterRegistry sseEmitterRegistry,
                      ObjectMapper objectMapper) {
        this.memberRepository = memberRepository;
        this.workSpaceRepository = workSpaceRepository;
        this.alarmRepository = alarmRepository;
        this.sseEmitterRegistry = sseEmitterRegistry;
        this.objectMapper = objectMapper;
        // heartbeat 스케줄러 시작 (25초마다 ping 이벤트 전송)
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::sendHeartbeatToAllEmitters, 25, 25, TimeUnit.SECONDS);
    }

    // ✅ SSE 연결
    public SseEmitter connect(Long userId) {
        SseEmitter sseEmitter = new SseEmitter(0L); // 무제한
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));

        sseEmitterRegistry.registerEmitter(member.getMemberId(), sseEmitter);

        sseEmitter.onCompletion(() -> {
            log.info("[SSE 연결 종료] memberId={}", member.getMemberId());
            sseEmitterRegistry.removeEmitter(member.getMemberId());
        });

        sseEmitter.onTimeout(() -> {
            log.warn("[SSE 타임아웃] memberId={}", member.getMemberId());
            sseEmitterRegistry.removeEmitter(member.getMemberId());
        });

        sseEmitter.onError((e) -> {
            log.error("[SSE 오류] memberId={}, error={}", member.getMemberId(), e.getMessage());
            sseEmitterRegistry.removeEmitter(member.getMemberId());
        });

        // ✅ 즉시 초기 연결 메시지 전송 (중요!)
        try {
            sseEmitter.send(SseEmitter.event()
                    .name("connect")
                    .data("SSE connected")
                    .reconnectTime(3000L));
            log.info("[SSE 연결 성공] memberId={}", member.getMemberId());
        } catch (IOException e) {
            log.error("[SSE 초기 메시지 전송 실패] memberId={}", member.getMemberId(), e);
            sseEmitterRegistry.removeEmitter(member.getMemberId());
            sseEmitter.completeWithError(e);
        }

        return sseEmitter;
    }

    // ✅ 특정 사용자에게 알림 전송
    public void sendToClient(AlarmResDto alarmResDto) {
        Member member = memberRepository.findById(Long.valueOf(alarmResDto.getReceiverId()))
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));
        WorkSpace workSpace = workSpaceRepository.findById(alarmResDto.getWorkSpaceSeq())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 프로젝트 또는 개인 워크스페이스입니다."));

        String data;
        try {
            data = objectMapper.writeValueAsString(alarmResDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("알림 직렬화 실패", e);
        }

        SseEmitter sseEmitter = sseEmitterRegistry.getEmitter(member.getMemberId());
        Alarm alarm = alarmRepository.save(alarmResDto.toEntity(member, workSpace, alarmResDto));

        if (sseEmitter != null) {
            try {
                sseEmitter.send(SseEmitter.event()
                        .name("alarm")
                        .data(AlarmResDto.fromEntity(alarm))
                        .id(String.valueOf(alarm.getAlarmSeq()))
                        .reconnectTime(3000L));
                log.info("[SSE] 알림 실시간 전송 성공 (to {})", member.getMemberId());
            } catch (IOException e) {
                log.warn("[SSE] 전송 실패, emitter 제거: {}", e.getMessage());
                sseEmitterRegistry.removeEmitter(member.getMemberId());
            }
        } else {
            log.info("[SSE] emitter 없음 — DB 저장만 수행 (receiverId={})", member.getMemberId());
        }
    }

    // ✅ Heartbeat (ping) 주기적 전송
    private void sendHeartbeatToAllEmitters() {
        try {
            Map<String, SseEmitter> emitters = sseEmitterRegistry.getAllEmitters();

            // ✅ INFO 레벨로 변경 + 상세 로그
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("[SSE Heartbeat] 💓 전송 시작");
            log.info("[SSE Heartbeat] 📊 등록된 emitter 수: {}", emitters.size());

            if (emitters.isEmpty()) {
                log.info("[SSE Heartbeat] ⚠️ 전송 대상 없음 (모든 emitter가 비어있음)");
                return;
            }

            int successCount = 0;
            int failCount = 0;

            for (Map.Entry<String, SseEmitter> entry : emitters.entrySet()) {
                String memberId = entry.getKey();
                SseEmitter emitter = entry.getValue();

                try {
                    emitter.send(SseEmitter.event()
                            .name("ping")
                            .data("keep-alive")
                            .reconnectTime(3000L));

                    log.info("[SSE] ✅ heartbeat 전송 성공 → {}", memberId);
                    successCount++;

                } catch (IOException e) {
                    log.warn("[SSE] ❌ heartbeat 실패 → emitter 제거 ({}): {}", memberId, e.getMessage());
                    sseEmitterRegistry.removeEmitter(memberId);
                    failCount++;
                }
            }

            log.info("[SSE Heartbeat] 📊 전송 완료 - 성공: {}, 실패: {}", successCount, failCount);
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        } catch (Exception e) {
            log.error("[SSE Heartbeat] ❌ 예외 발생: {}", e.getMessage(), e);
        }
    }

    // 멤버 상태 변경(오프라인 / 온라인 / 자리비움)
    public void changeMemberStatus(MemberStatusResDto memberStatusResDto) throws IOException {
        // 멤버 정보 가져오기
        Member member = memberRepository.findById(memberStatusResDto.getMemberSeq()).orElseThrow(()
                -> new EntityNotFoundException("존재하지 않는 회원입니다."));
        String data = "";
        try {
            data = objectMapper.writeValueAsString(memberStatusResDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        // emitter 객체를 통해 메시지 전송
        SseEmitter sseEmitter = sseEmitterRegistry.getEmitter(member.getMemberId());
        try {
            sseEmitter.send(data);
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    // pub&sub한 메시지 확인
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