package com.kss.astrologer.repository;

import com.kss.astrologer.models.CallSession;
import com.kss.astrologer.types.ChatStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CallSessionRepository extends JpaRepository<CallSession, UUID> {
    Optional<CallSession> findByAstrologerIdAndStatus(UUID astrologerId, ChatStatus chatStatus);

    Page<CallSession> findByUserIdOrAstrologerId(UUID userId, UUID astrologerId, Pageable pageable);

    List<CallSession> findByStatus(ChatStatus chatStatus);
}
