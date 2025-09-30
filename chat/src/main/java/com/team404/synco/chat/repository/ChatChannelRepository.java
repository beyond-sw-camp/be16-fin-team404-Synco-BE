package com.team404.synco.chat.repository;

import com.team404.synco.chat.entity.ChatChannel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatChannelRepository extends JpaRepository<ChatChannel, Long> {
}
