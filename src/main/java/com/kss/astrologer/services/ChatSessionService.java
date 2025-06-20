package com.kss.astrologer.services;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kss.astrologer.dto.ChatQueueEntry;
import com.kss.astrologer.exceptions.CustomException;
import com.kss.astrologer.models.AstrologerDetails;
import com.kss.astrologer.models.ChatSession;
import com.kss.astrologer.models.User;
import com.kss.astrologer.models.Wallet;
import com.kss.astrologer.repository.AstrologerRepository;
import com.kss.astrologer.repository.ChatSessionRepository;
import com.kss.astrologer.repository.UserRepository;
import com.kss.astrologer.repository.WalletRepository;
import com.kss.astrologer.types.ChatStatus;

@Service
public class ChatSessionService {

    @Autowired
    private ChatSessionRepository sessionRepo;

    @Autowired
    private ChatQueueService queueService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AstrologerRepository astrologerRepository;

    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private ObjectMapper objectMapper;

    public String requestChat(UUID userId, UUID astrologerId, int requestedMinutes) {
        // AstrologerDetails astrologer = astrologerRepository.findById(astrologerId)
        //         .orElseThrow(() -> new CustomException("Astrologer not found"));
        Optional<ChatSession> activeSession = sessionRepo.findByAstrologerIdAndStatus(astrologerId, ChatStatus.ACTIVE);

        if (activeSession.isEmpty()) {
            return startChat(userId, astrologerId, requestedMinutes).getId().toString();
        } else {
            queueService.enqueue(astrologerId, userId, requestedMinutes);
            long pos = queueService.getPosition(astrologerId, userId);
            return "Astrologer is busy. You are in queue at position: " + (pos + 1);
        }
    }

    @Transactional
    public ChatSession startChat(UUID userId, UUID astrologerId, int requestedMinutes) {
        if (requestedMinutes < 5) {
            throw new IllegalArgumentException("Minimum chat duration is 5 minutes.");
        }

        User user = userRepository.findById(userId).orElseThrow();
        AstrologerDetails astrologer = astrologerRepository.findByUserId(astrologerId).orElseThrow();

        Double perMinuteRate = astrologer.getPricePerMinuteChat();
        Double totalCharge = perMinuteRate * requestedMinutes;

        Wallet wallet = user.getWallet();

        if (wallet.getBalance().compareTo(totalCharge) < 0) {
            throw new CustomException("Not enough balance for " + requestedMinutes + " minutes.");
        }

        // Deduct wallet amount
        wallet.setBalance(wallet.getBalance() - totalCharge);
        walletRepository.save(wallet);

        ChatSession session = ChatSession.builder()
                .user(user)
                .astrologer(astrologer.getUser())
                .startedAt(LocalDateTime.now())
                .status(ChatStatus.ACTIVE)
                // .perMinuteRate(perMinuteRate)
                .totalCost(totalCharge)
                .totalMinutes(requestedMinutes)
                .build();

        return sessionRepo.save(session);
    }

    public void endChat(UUID sessionId) {
        ChatSession session = sessionRepo.findById(sessionId).orElseThrow();
        session.setEndedAt(LocalDateTime.now());
        session.setStatus(ChatStatus.ENDED);

        sessionRepo.save(session);

        // Trigger next user from queue
        UUID astrologerId = session.getAstrologer().getId();
        String nextUser = queueService.dequeue(astrologerId);
        if (nextUser != null) {
            try {
                ChatQueueEntry entry = objectMapper.readValue(nextUser, ChatQueueEntry.class);
                startChat(entry.getUserId(), astrologerId, entry.getRequestedMinutes());
                // TODO: Notify user via WebSocket
            } catch (Exception e) {
                throw new CustomException("Failed to start chat for next user in queue");
            }
        }
    }

    public ChatSession getSessionById(UUID sessionId) {
        return sessionRepo.findById(sessionId)
                .orElseThrow(() -> new CustomException("Chat session not found"));
    }

    
}
