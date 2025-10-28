package com.team404.synco.alarm.service;

import com.team404.synco.alarm.dto.AlarmResDto;
import com.team404.synco.alarm.entity.Alarm;
import com.team404.synco.alarm.repository.AlarmRepository;
import com.team404.synco.common.constant.AlarmType;
import com.team404.synco.common.constant.YnColumn;
import com.team404.synco.common.service.SseService;
import com.team404.synco.member.entity.Member;
import com.team404.synco.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AlarmService {
    private final AlarmRepository alarmRepository;
    private final MemberRepository memberRepository;
    private final SseService sseService;

    // 알림 생성(내부 서비스 : 프로젝트, 친구)
    public void createAlarm(Long memberSeq, String alarmType, String message){
        // 전달 대상자가 있는지 확인
        Member member = memberRepository.findById(memberSeq).orElseThrow(()
                -> new EntityNotFoundException("존재하지 않는 회원입니다."));

        // 알림 데이터 조립
        AlarmResDto alarmResDto = AlarmResDto.of(member.getMemberId(), alarmType, message);

//        WorkSpace workSpace = workSpaceRepository.findById(alarmContentResDto.get()).orElseThrow(()
//                -> new EntityNotFoundException("존재하지 않는 프로젝트입니다."));
        try{
            sseService.sendToClient(alarmResDto);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 알림 목록 조회
    public List<AlarmResDto> findMyAlarmList(Long memberSeq) {
        return alarmRepository.findAllByMemberSeq(memberSeq).stream()
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
        alarmRepository.findAllByMemberSeqAndYnRead(memberSeq, YnColumn.IS_FALSE)
                .forEach(Alarm::updateReadStatus);
    }

    // 특정 그룹 알림 모두 읽음 처리
    public void readAlarmListByType(Long memberSeq, AlarmType alarmType){
        alarmRepository.findAllByMemberSeqAndYnRead(memberSeq, YnColumn.IS_FALSE)
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
