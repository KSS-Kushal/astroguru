package com.kss.astrologer.services.schedulers;

import com.kss.astrologer.models.CallSession;
import com.kss.astrologer.models.ChatSession;
import com.kss.astrologer.repository.CallSessionRepository;
import com.kss.astrologer.repository.ChatSessionRepository;
import com.kss.astrologer.types.ChatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SessionMonitorScheduler {

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private CallSessionRepository callSessionRepository;

    @Scheduled(cron = "0 0 */2 * * *") // Every 2 hours
    public void autoEndExpiredSessions() {
        endExpiredCallSessions();
        endExpiredChatSessions();
    }

    private void endExpiredCallSessions() {
        List<CallSession> activeSessions = callSessionRepository.findByStatus(ChatStatus.ACTIVE);
        LocalDateTime now = LocalDateTime.now();

        for (CallSession session : activeSessions) {
            LocalDateTime expectedEnd = session.getStartedAt().plusMinutes(session.getTotalMinutes());
            if (expectedEnd.isBefore(now)) {
                session.setEndedAt(expectedEnd);
                session.setStatus(ChatStatus.ENDED);
            }
        }
        callSessionRepository.saveAll(activeSessions);
    }

    private void endExpiredChatSessions() {
        List<ChatSession> activeSessions = chatSessionRepository.findByStatus(ChatStatus.ACTIVE);
        LocalDateTime now = LocalDateTime.now();

        for (ChatSession session : activeSessions) {
            LocalDateTime expectedEnd = session.getStartedAt().plusMinutes(session.getTotalMinutes());
            if (expectedEnd.isBefore(now)) {
                session.setEndedAt(expectedEnd);
                session.setStatus(ChatStatus.ENDED);
            }
        }
        chatSessionRepository.saveAll(activeSessions);
    }
}
