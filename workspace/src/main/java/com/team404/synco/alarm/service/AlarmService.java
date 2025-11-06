package com.team404.synco.alarm.service;

import com.team404.synco.alarm.dto.AlarmFindReqDto;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void createAlarm(AlarmResDto alarmResDto) {
        WorkSpace workSpace = workSpaceRepository.findById(alarmResDto.getWorkSpaceSeq())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 프로젝트 또는 개인 워크스페이스입니다."));

        Member member = memberRepository.findById(Long.valueOf(alarmResDto.getReceiverId()))
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));


        if(!YnColumn.IS_FALSE.equals(member.getYnAlarmOffSet()) || alarmResDto.getAlarmType().equals(AlarmType.CHAT)){
            try {
                // DB 저장 시도
                Alarm alarm = alarmRepository.save(alarmResDto.toEntity(member, workSpace, alarmResDto));

                // 저장 성공 시에만 SSE 전송
                sseService.sendToClient(AlarmResDto.fromEntity(alarm));

            } catch (DataAccessException e) {
                // DB 관련 예외 (JPA, JDBC, Hibernate 등)
                throw new RuntimeException("알림 저장 중 오류가 발생했습니다.", e);
            } catch (Exception e) {
                // 기타 예외 처리
                throw new RuntimeException("알림 생성 중 오류가 발생했습니다.", e);
            }
        }
    }

    // 알림 목록 조회
    @Transactional(readOnly = true)
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
        alarmRepository.deleteByMember(member);
    }

    // 알림 삭제 처리(프로젝트 모두)
    public void deleteAllProjectAlarm(Long memberSeq, AlarmFindReqDto alarmFindReqDto) {
        Member member = memberRepository.findById(memberSeq).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));
        WorkSpace workSpace = workSpaceRepository.findById(alarmFindReqDto.getWorkSpaceSeq()).orElseThrow(() ->
                new EntityNotFoundException("존재하지 않는 워크스페이스입니다."));
        alarmRepository.deleteByMemberAndWorkSpace(member, workSpace);
    }

    // 특정 그룹 알림 모두 삭제(개인)
    public void deleteAllPersonalAlarmByType(Long memberSeq, AlarmFindReqDto alarmFindReqDto){
        Member member = memberRepository.findById(memberSeq).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 회원입니다."));
        alarmRepository.deleteByMemberAndAlarmType(member, alarmFindReqDto.getAlarmType());
    }

    // 특정 그룹 알림 모두 삭제(프로젝트)
    public void deleteAllProjectAlarmByType(Long memberSeq, AlarmFindReqDto alarmFindReqDto){
        Member member = memberRepository.findById(memberSeq).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 회원입니다."));
        WorkSpace workSpace = workSpaceRepository.findById(alarmFindReqDto.getWorkSpaceSeq()).orElseThrow(() ->
                new EntityNotFoundException("존재하지 않는 워크스페이스입니다."));
        alarmRepository.deleteByMemberAndWorkSpaceAndAlarmType(member, workSpace, alarmFindReqDto.getAlarmType());
    }
}