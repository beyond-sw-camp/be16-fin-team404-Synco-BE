package com.team404.synco.alarm.service;

import com.team404.synco.alarm.dto.AlarmResDto;
import com.team404.synco.alarm.entity.Alarm;
import com.team404.synco.alarm.repository.AlarmRepository;
import com.team404.synco.common.constant.AlarmType;
import com.team404.synco.common.constant.YnColumn;
import com.team404.synco.common.service.SseService;
import com.team404.synco.member.entity.Member;
import com.team404.synco.member.repository.MemberRepository;
import com.team404.synco.workspace.entity.WorkSpace;
import com.team404.synco.workspace.repository.WorkSpaceRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AlarmService {
    private final AlarmRepository alarmRepository;
    private final MemberRepository memberRepository;
    private final WorkSpaceRepository workSpaceRepository;
    private final SseService sseService;

    // 알림 생성(내부 서비스 : 프로젝트, 친구)
    public void createAlarm(Long memberSeq, String alarmType, String message, Long workSpaceSeq){

        WorkSpace workSpace = workSpaceRepository.findById(workSpaceSeq).orElseThrow(()
                -> new EntityNotFoundException("존재하지 않는 프로젝트 또는 개인 워크스페이스입니다."));

        // 알림 데이터 조립
        AlarmResDto alarmResDto = AlarmResDto.of(String.valueOf(memberSeq), alarmType, message, workSpace.getWorkSpaceSeq());
        log.info("멤버 ID : " + memberSeq);

        try{
            sseService.sendToClient(alarmResDto);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 온/오프라인
    // 알림 목록 조회
    public List<AlarmResDto> findMyAlarmList(Long memberSeq) {
        Member member = memberRepository.findById(memberSeq).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 회원입니다."));
        return alarmRepository.findAllByMember(member).stream()
                .map(AlarmResDto::fromEntity)
                .toList();
    }

    // 알림 읽음 처리(단건)
    public void readAlarm(Long alarmSeq){
        Alarm alarm = alarmRepository.findById(alarmSeq).orElseThrow(() -> new EntityNotFoundException("해당 알림이 존재하지 않습니다."));
        alarm.updateReadStatus();
    }

    // 알림 읽음 처리(모두)
    public void readAllAlarm(Long memberSeq){
        Member member = memberRepository.findById(memberSeq).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 회원입니다."));
        alarmRepository.findAllByMemberAndYnRead(member, YnColumn.IS_FALSE)
                .forEach(Alarm::updateReadStatus);
    }

    // 특정 그룹 알림 모두 읽음 처리
    public void readAlarmListByType(Long memberSeq, AlarmType alarmType){
        Member member = memberRepository.findById(memberSeq).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 회원입니다."));
        alarmRepository.findAllByMemberAndYnRead(member, YnColumn.IS_FALSE)
                .forEach(Alarm::updateReadStatus);
    }

    // 알림 삭제(단건)
    public void deleteAlarm(Long alarmSeq){
        alarmRepository.deleteById(alarmSeq);
    }


    // 알림 삭제(모두)
    public void deleteAllAlarm(){

    }

    // 특정 그룹 알림 모두 삭제
    public void deleteAlarmListByType(Long memberSeq, AlarmType alarmType){

    }
}
