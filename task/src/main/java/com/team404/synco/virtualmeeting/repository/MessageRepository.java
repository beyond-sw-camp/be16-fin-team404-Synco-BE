package com.team404.synco.virtualmeeting.repository;

import com.team404.synco.virtualmeeting.dto.Room.ChatMessageRes;
import com.team404.synco.virtualmeeting.entity.Message;
import com.team404.synco.virtualmeeting.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findAllByRoom(Room room, Pageable pageable);
}
