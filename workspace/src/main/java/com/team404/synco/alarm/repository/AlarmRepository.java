package com.team404.synco.alarm.repository;

import com.team404.synco.alarm.entity.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    List<Alarm> findAllByMemberSeq(Long memberSeq);

    List<Alarm> findAllByMemberSeqAndYnRead(Long memberSeq, String isFalse);
}
