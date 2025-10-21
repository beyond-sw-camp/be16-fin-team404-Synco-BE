package com.team404.synco.task.repository;

import com.team404.synco.task.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    @Query("SELECT b FROM Board b " +
            "JOIN FETCH b.taskList t " +
            "JOIN FETCH b.scheduleManagementChannelMember smcm " +
            "WHERE smcm.memberSeq = :memberSeq " +
            "AND smcm.workSpaceSeq = :workSpaceSeq")
    List<Board> findAllByMemberAndWorkSpace(long memberSeq, long workSpaceSeq);

    @Query("SELECT MAX(b.orders) FROM Board b WHERE b.scheduleManagementChannelMember.workSpaceSeq = :workSpaceSeq")
    Optional<Long> findMaxOrderByWorkSpaceSeq(long workSpaceSeq);

}
