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
    }

    // SSE 연결
    public SseEmitter connect(Long userId) {
        SseEmitter sseEmitter = new SseEmitter(60L * 1000 * 10);

        // 멤버 정보 가져오기
        Member member = memberRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));
        sseEmitterRegistry.registerEmitter(member.getMemberId(), sseEmitter);

        sseEmitter.onCompletion(() -> sseEmitterRegistry.removeEmitter(member.getMemberId()));
        sseEmitter.onTimeout(() -> sseEmitterRegistry.removeEmitter(member.getMemberId()));

        log.info("[SSE 연결] userId={}", member.getMemberId());
        return sseEmitter;
    }

    // 특정 사용자에게 message 발송(알림)
    public void sendToClient(AlarmResDto alarmResDto) throws IOException {
        // 수신 멤버 정보 가져오기
        Member member = memberRepository.findById(Long.valueOf(alarmResDto.getReceiverId())).orElseThrow(()
                -> new EntityNotFoundException("존재하지 않는 회원입니다."));
        log.info("[SSE] 수신 멤버 정보 가져옴");
        WorkSpace workSpace = workSpaceRepository.findById(alarmResDto.getWorkSpaceSeq()).orElseThrow(()
                -> new EntityNotFoundException("존재하지 않는 프로젝트 또는 개인 워크스페이스입니다."));
        // 워크스페이스 정보 가져오기
        String data = "";
        try {
            data = objectMapper.writeValueAsString(alarmResDto);
            log.info("[SSE] 알람 직렬화");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        // emitter 객체를 통해 메시지 전송
        SseEmitter sseEmitter = sseEmitterRegistry.getEmitter(member.getMemberId());
        log.info("[SSE] 에미터 가져옴");
        try {
            Alarm alarm = alarmRepository.save(alarmResDto.toEntity(member, workSpace, alarmResDto));
            sseEmitter.send(AlarmResDto.fromEntity(alarm));
            log.info("알림 전송 성공");
        } catch (IOException e) {
            throw new IOException(e);
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