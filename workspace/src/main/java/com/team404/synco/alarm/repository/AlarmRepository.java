package com.team404.synco.alarm.repository;

import com.team404.synco.alarm.entity.Alarm;
import com.team404.synco.common.constant.AlarmType;
import com.team404.synco.member.entity.Member;
import com.team404.synco.workspace.entity.WorkSpace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    List<Alarm> findAllByMemberOrderByAlarmSeqDesc(Member member);
    List<Alarm> findAllByMemberAndYnRead(Member member, String isFalse);
    List<Alarm> findAllByMemberAndYnReadAndAlarmType(Member member, String isFalse, AlarmType alarmType);
    List<Alarm> findAllByMemberAndWorkSpaceAndYnRead(Member member, WorkSpace workSpace, String isFalse);
    List<Alarm> findAllByMemberAndWorkSpaceAndYnReadAndAlarmType(Member member, WorkSpace workSpace, String isFalse, AlarmType alarmType);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Alarm a WHERE a.member = :member AND a.alarmType = alarmType")
    void deleteByMemberAndAlarmType(@Param("member") Member member, @Param("alarmType")AlarmType alarmType);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Alarm a WHERE a.member = :member AND a.workSpace = :workSpace AND a.alarmType = :alarmType")
    void deleteByMemberAndWorkSpaceAndAlarmType(@Param("member") Member member, @Param("workSpace") WorkSpace workSpace, @Param("alarmType")AlarmType alarmType);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Alarm a WHERE a.member = :member")
    void deleteByMember(@Param("member") Member member);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Alarm a WHERE a.member = :member AND a.workSpace = :workSpace")
    void deleteByMemberAndWorkSpace(@Param("member")Member member, @Param("workSpace")  WorkSpace workSpace);
}