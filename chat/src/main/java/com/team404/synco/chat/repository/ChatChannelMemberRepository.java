package com.team404.synco.chat.repository;

import com.team404.synco.chat.entity.ChatChannelMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatChannelMemberRepository extends JpaRepository<ChatChannelMember, Long> {
}
