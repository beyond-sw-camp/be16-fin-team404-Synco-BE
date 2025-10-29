package com.team404.synco.alarm.repository;

import com.team404.synco.alarm.entity.Alarm;
import com.team404.synco.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    List<Alarm> findAllByMember(Member member);

    List<Alarm> findAllByMemberAndYnRead(Member member, String isFalse);
}
