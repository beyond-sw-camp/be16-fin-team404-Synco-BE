package com.team404.synco.task.repository;

import com.team404.synco.task.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    @Query("SELECT b FROM Board b " +
            "LEFT JOIN FETCH b.taskList t " +
            "JOIN FETCH b.scheduleManagementChannelMember smcm " +
            "WHERE smcm.memberSeq = :memberSeq " +
            "AND smcm.workSpaceSeq = :workSpaceSeq " +
            "ORDER BY b.orders ASC")
    List<Board> findAllByMemberAndWorkSpace(long memberSeq, long workSpaceSeq);

    @Query("SELECT MAX(b.orders) FROM Board b WHERE b.scheduleManagementChannelMember.memberSeq = :memberSeq AND b.scheduleManagementChannelMember.workSpaceSeq = :workSpaceSeq")
    Optional<Long> findMaxOrderByMemberAndWorkSpace(long memberSeq, long workSpaceSeq);

    @Query("SELECT b FROM Board b WHERE b.scheduleManagementChannelMember.memberSeq = :memberSeq " +
           "AND b.scheduleManagementChannelMember.workSpaceSeq = :workSpaceSeq " +
           "AND b.orders > :deletedOrder " +
           "ORDER BY b.orders ASC")
    List<Board> findBoardsWithOrdersGreaterThan(long memberSeq, long workSpaceSeq, long deletedOrder);

    @Modifying
    @Query("UPDATE Board b SET b.orders = :newOrders WHERE b.boardSeq = :boardSeq")
    void updateBoardOrders(@Param("boardSeq") long boardSeq, @Param("newOrders") long newOrders);

}