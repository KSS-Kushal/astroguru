package com.kss.astrologer.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kss.astrologer.models.ChatSession;
import com.kss.astrologer.types.ChatStatus;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {
    Optional<ChatSession> findByAstrologerIdAndStatus(UUID astrologerId, ChatStatus status);
}
