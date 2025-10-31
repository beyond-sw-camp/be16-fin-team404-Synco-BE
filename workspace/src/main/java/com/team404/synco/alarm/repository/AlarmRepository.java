package com.team404.synco.alarm.repository;

import com.team404.synco.alarm.entity.Alarm;
import com.team404.synco.common.constant.AlarmType;
import com.team404.synco.member.entity.Member;
import com.team404.synco.workspace.entity.WorkSpace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    List<Alarm> findAllByMemberOrderByAlarmSeqDesc(Member member);
    List<Alarm> findAllByMemberAndYnRead(Member member, String isFalse);
    List<Alarm> findAllByMemberAndYnReadAndAlarmType(Member member, String isFalse, AlarmType alarmType);
    List<Alarm> findAllByMemberAndWorkSpaceAndYnRead(Member member, WorkSpace workSpace, String isFalse);
    List<Alarm> findAllByMemberAndWorkSpaceAndYnReadAndAlarmType(Member member, WorkSpace workSpace, String isFalse, AlarmType alarmType);
    Iterable<Alarm> deleteAllByMemberAndAlarmType(Member member, AlarmType alarmType);
    Iterable<Alarm> deleteAllByMemberAndWorkSpaceAndAlarmType(Member member, WorkSpace workSpace, AlarmType alarmType);   Iterable<Alarm> deleteAllByMemberAndYnRead(Member member, String isFalse);
    Iterable<Alarm> deleteAllByMemberAndWorkSpaceAndYnRead(Member member, WorkSpace workSpace, String isFalse);
}