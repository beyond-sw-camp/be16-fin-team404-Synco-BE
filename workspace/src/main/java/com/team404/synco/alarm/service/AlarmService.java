package com.team404.synco.alarm.service;

import com.team404.synco.alarm.dto.AlarmFindReqDto;
import com.team404.synco.alarm.dto.AlarmResDto;
import com.team404.synco.alarm.entity.Alarm;
import com.team404.synco.alarm.repository.AlarmRepository;
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
    public void createAlarm(Long memberSeq, String alarmType, String message, Long workSpaceSeq, Long targetSeq){

        WorkSpace workSpace = workSpaceRepository.findById(workSpaceSeq).orElseThrow(()
                -> new EntityNotFoundException("존재하지 않는 프로젝트 또는 개인 워크스페이스입니다."));

        // 알림 데이터 조립
        AlarmResDto alarmResDto = AlarmResDto.of(String.valueOf(memberSeq), alarmType, message, workSpace.getWorkSpaceSeq(), targetSeq);
        log.info("멤버 ID : " + memberSeq);

        sseService.sendToClient(alarmResDto);
    }

    // 온/오프라인
    // 알림 목록 조회
    public List<AlarmResDto> findMyAlarmList(Long memberSeq) {
        Member member = memberRepository.findById(memberSeq).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 회원입니다."));
        return alarmRepository.findAllByMemberOrderByAlarmSeqDesc(member).stream()
                .map(AlarmResDto::fromEntity)
                .toList();
    }

    // 알림 읽음 처리(단건)
    public void readAlarm(Long alarmSeq){
        Alarm alarm = alarmRepository.findById(alarmSeq).orElseThrow(() -> new EntityNotFoundException("해당 알림이 존재하지 않습니다."));
        alarm.updateReadStatus();
    }

    // 알림 읽음 처리(개인 모두)
    public void readAllPersonalAlarm(Long memberSeq){
        Member member = memberRepository.findById(memberSeq).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 회원입니다."));
        alarmRepository.findAllByMemberAndYnRead(member, YnColumn.IS_FALSE)
                .forEach(Alarm::updateReadStatus);
    }

    // 알림 읽음 처리(프로젝트 모두)
    public void readAllProjectAlarm(Long memberSeq, AlarmFindReqDto alarmFindReqDto){
        Member member = memberRepository.findById(memberSeq).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 회원입니다."));
        WorkSpace workSpace = workSpaceRepository.findById(alarmFindReqDto.getWorkSpaceSeq()).orElseThrow(() ->
                new EntityNotFoundException("존재하지 않는 워크스페이스입니다."));
        alarmRepository.findAllByMemberAndWorkSpaceAndYnRead(member, workSpace, YnColumn.IS_FALSE)
                .forEach(Alarm::updateReadStatus);
    }

    // 특정 그룹 알림 모두 읽음 처리(개인)
    public void readAllPersonalAlarmByType(Long memberSeq, AlarmFindReqDto alarmFindReqDto){
        Member member = memberRepository.findById(memberSeq).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 회원입니다."));
        alarmRepository.findAllByMemberAndYnReadAndAlarmType(member, YnColumn.IS_FALSE, alarmFindReqDto.getAlarmType())
                .forEach(Alarm::updateReadStatus);
    }

    // 특정 그룹 알림 모두 읽음 처리(프로젝트)
    public void readAllProjectAlarmByType(Long memberSeq, AlarmFindReqDto alarmFindReqDto){
        Member member = memberRepository.findById(memberSeq).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 회원입니다."));
        WorkSpace workSpace = workSpaceRepository.findById(alarmFindReqDto.getWorkSpaceSeq()).orElseThrow(() ->
                new EntityNotFoundException("존재하지 않는 워크스페이스입니다."));
        alarmRepository.findAllByMemberAndWorkSpaceAndYnReadAndAlarmType(member, workSpace, YnColumn.IS_FALSE,
                alarmFindReqDto.getAlarmType())
                .forEach(Alarm::updateReadStatus);
    }

    // 알림 삭제(단건)
    public void deleteAlarm(Long alarmSeq){
        alarmRepository.deleteById(alarmSeq);
    }

    // 알림 삭제 처리(개인 모두)
    public void deleteAllPersonalAlarm(Long memberSeq){
        Member member = memberRepository.findById(memberSeq).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 회원입니다."));
        alarmRepository.deleteAllByMemberAndYnRead(member, YnColumn.IS_FALSE)
                .forEach(Alarm::updateReadStatus);
    }

    // 알림 삭제 처리(프로젝트 모두)
    public void deleteAllProjectAlarm(Long memberSeq, AlarmFindReqDto alarmFindReqDto){
        Member member = memberRepository.findById(memberSeq).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 회원입니다."));
        WorkSpace workSpace = workSpaceRepository.findById(alarmFindReqDto.getWorkSpaceSeq()).orElseThrow(() ->
                new EntityNotFoundException("존재하지 않는 워크스페이스입니다."));
        alarmRepository.deleteAllByMemberAndWorkSpaceAndYnRead(member, workSpace, YnColumn.IS_FALSE)
                .forEach(Alarm::updateReadStatus);
    }

    // 특정 그룹 알림 모두 삭제(개인)
    public void deleteAllPersonalAlarmByType(Long memberSeq, AlarmFindReqDto alarmFindReqDto){
        Member member = memberRepository.findById(memberSeq).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 회원입니다."));
        alarmRepository.deleteAllByMemberAndAlarmType(member, alarmFindReqDto.getAlarmType())
                .forEach(Alarm::updateReadStatus);
    }

    // 특정 그룹 알림 모두 삭제(프로젝트)
    public void deleteAllProjectAlarmByType(Long memberSeq, AlarmFindReqDto alarmFindReqDto){
        Member member = memberRepository.findById(memberSeq).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 회원입니다."));
        WorkSpace workSpace = workSpaceRepository.findById(alarmFindReqDto.getWorkSpaceSeq()).orElseThrow(() ->
                new EntityNotFoundException("존재하지 않는 워크스페이스입니다."));
        alarmRepository.deleteAllByMemberAndWorkSpaceAndAlarmType(member, workSpace, alarmFindReqDto.getAlarmType())
                .forEach(Alarm::updateReadStatus);
    }
}
