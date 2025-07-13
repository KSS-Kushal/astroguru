package com.kss.astrologer.services;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import com.kss.astrologer.types.SessionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kss.astrologer.dto.ChatQueueEntry;
import com.kss.astrologer.dto.ChatSessionDto;
import com.kss.astrologer.dto.UserDto;
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

    private static final Logger logger = LoggerFactory.getLogger(ChatSessionService.class);

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
    private WalletService walletService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private TaskScheduler taskScheduler;

    private final Map<UUID, ScheduledFuture<?>> timerTasks = new ConcurrentHashMap<>();


    public long requestChat(UUID userId, UUID astrologerId, int requestedMinutes) {
        if (requestedMinutes < 5 && requestedMinutes != 2) {
            throw new CustomException("Minimum chat duration is 5 minutes.");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException("User not found"));
        AstrologerDetails astrologer = astrologerRepository.findByUserId(astrologerId)
                .orElseThrow(() -> new CustomException("Astrologer not found"));

        boolean isFreeChat = !user.isFreeChatUsed();
        Double perMinuteRate = astrologer.getPricePerMinuteChat();
        Wallet wallet = walletService.getWalletByUserId(userId);
        if (wallet == null) {
            throw new CustomException("Wallet not found for user ID: " + userId);
        }
        Double totalCharge = isFreeChat ? 0.0 : perMinuteRate * requestedMinutes;

        if (wallet.getBalance().compareTo(totalCharge) < 0) {
            throw new CustomException("Not enough balance for " + requestedMinutes + " minutes.");
        }
        queueService.enqueue(astrologerId, userId, requestedMinutes, SessionType.CHAT);
        long pos = queueService.getPosition(astrologerId, userId);
        messagingTemplate.convertAndSend("/topic/queue/" + astrologerId, "New chat request received");
        return pos + 1;
    }

    public String acceptChat(UUID userId, UUID astrologerId) {
        if (!queueService.isNextInQueue(astrologerId, userId)) {
            return "User is not first in the queue.";
        }

        String entry = queueService.dequeue(astrologerId);
        int duration = queueService.parseRequestedMinutes(entry);

        startChat(userId, astrologerId, duration);

        return "Chat accepted and started.";
    }

    @Transactional
    public void startChat(UUID userId, UUID astrologerId, int requestedMinutes) {
        if (requestedMinutes < 5 && requestedMinutes != 2) {
            throw new CustomException("Minimum chat duration is 5 minutes.");
        }

        Optional<ChatSession> existing = sessionRepo.findByAstrologerIdAndStatus(astrologerId, ChatStatus.ACTIVE);
        if (existing.isPresent()) {
            logger.warn("Chat already active for astrologer: {}", astrologerId);
            return; // Prevent duplicate start
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException("User not found"));
        AstrologerDetails astrologer = astrologerRepository.findByUserId(astrologerId)
                .orElseThrow(() -> new CustomException("Astrologer not found"));

        boolean isFreeChat = !user.isFreeChatUsed();
        Double perMinuteRate = astrologer.getPricePerMinuteChat();
        Double totalCharge = isFreeChat ? 0.0 : perMinuteRate * requestedMinutes;

        Wallet wallet = user.getWallet();
        // Wallet astrologerWallet = astrologer.getUser().getWallet();

        if (wallet == null) {
            wallet = new Wallet();
            wallet.setBalance(0.0);
            wallet.setUser(user);
            wallet = walletRepository.save(wallet);
            user.setWallet(wallet);
            userRepository.save(user);
        }

        if (wallet.getBalance().compareTo(totalCharge) < 0) {
            throw new CustomException("Not enough balance for " + requestedMinutes + " minutes.");
        }

        if(isFreeChat) {
            user.setFreeChatUsed(true);
            userRepository.save(user);
        }

        // Deduct wallet amount from user
        // WalletTransaction transaction = new WalletTransaction();
        // transaction.setAmount(totalCharge);
        // transaction.setType(TransactionType.DEBIT);
        // transaction.setWallet(wallet);
        // transaction.setDescription("Chat session with astrologer " + astrologer.getUser().getName() + " for "
        //         + requestedMinutes + " minutes.");

        // transaction = walletTransactionRepository.save(transaction);

        // wallet.setBalance(wallet.getBalance() - totalCharge);
        // List<WalletTransaction> transactions = wallet.getTransactions();
        // transactions.add(transaction);
        // wallet.setTransactions(transactions);
        // walletRepository.save(wallet);
        walletService.debitBalance(userId, totalCharge, "Chat session with astrologer " + astrologer.getUser().getName() + " for "
                + requestedMinutes + " minutes.");

        // Credit amount to astrologer's wallet
        // WalletTransaction astrologerTransaction = new WalletTransaction();
        // astrologerTransaction.setAmount(totalCharge);
        // astrologerTransaction.setType(TransactionType.CREDIT);
        // astrologerTransaction.setWallet(astrologerWallet);
        // astrologerTransaction
        //         .setDescription("Chat session with user " + user.getName() + " for " + requestedMinutes + " minutes.");
        // astrologerTransaction = walletTransactionRepository.save(astrologerTransaction);

        // astrologerWallet.setBalance(astrologerWallet.getBalance() + totalCharge);
        // List<WalletTransaction> astrologerTransactions = astrologerWallet.getTransactions();
        // astrologerTransactions.add(astrologerTransaction);
        // astrologerWallet.setTransactions(astrologerTransactions);
        // walletRepository.save(astrologerWallet);
        walletService.creditBalance(astrologerId, totalCharge, "Chat session with user " + user.getName() + " for " + requestedMinutes + " minutes.");

        ChatSession session = ChatSession.builder()
                .user(user)
                .astrologer(astrologer.getUser())
                .startedAt(LocalDateTime.now())
                .status(ChatStatus.ACTIVE)
                .totalCost(totalCharge)
                .totalMinutes(requestedMinutes)
                .messages(List.of())
                .build();

        ChatSession createdSession = sessionRepo.save(session);
        ChatSessionDto createdSessionDto = new ChatSessionDto(createdSession);
        messagingTemplate.convertAndSend("/topic/chat/" + userId + "/chatId", createdSessionDto);
        messagingTemplate.convertAndSend("/topic/chat/" + astrologerId + "/chatId", createdSessionDto);

        startTimer(createdSession.getId(), createdSession.getTotalMinutes());
    }

    public void endChat(UUID sessionId) {
        ChatSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new CustomException("Session not found"));
        session.setEndedAt(LocalDateTime.now());
        session.setStatus(ChatStatus.ENDED);

        sessionRepo.save(session);

        try {
            messagingTemplate.convertAndSend("/topic/chat/" + session.getId(),
                    objectMapper.writeValueAsString(Map.of("status", "ended")));
        } catch (Exception e) {
            logger.error("Error to send msg end notification: ", e);
        }

        // Trigger next user from queue
        UUID astrologerId = session.getAstrologer().getId();
        String nextUser = queueService.peek(astrologerId);
        if (nextUser != null) {
            messagingTemplate.convertAndSend("/topic/queue/" + astrologerId, "Chat ended, next user can be served");
            // UUID userId = queueService.parseUserId(nextUser);
            // int requestedMinutes = queueService.parseRequestedMinutes(nextUser);
            // startChat(userId, astrologerId, requestedMinutes);
        }
    }

    public ChatSession getSessionById(UUID sessionId) {
        return sessionRepo.findById(sessionId)
                .orElseThrow(() -> new CustomException("Chat session not found"));
    }

    public List<UserDto> getRequestList(UUID astrologerId) {
        List<ChatQueueEntry> queue = queueService.getQueue(astrologerId);
        return queue.stream()
                .map(entry -> {
                    User user = userRepository.findById(entry.getUserId())
                            .orElseThrow(() -> new CustomException("User not found"));
                    return new UserDto(user);
                })
                .toList();
    }

    public long removeAllUserFromQueue(UUID astrologerId) {
        long length = queueService.getQueueLength(astrologerId);
        for (long i = 0; i < length; i++) {
            queueService.dequeue(astrologerId);
        }
        return length;
    }

    public Page<ChatSessionDto> getHistory(UUID userId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page-1, size, Direction.DESC, "startedAt");
        Page<ChatSession> sessions = sessionRepo.findByUserIdOrAstrologerId(userId, userId, pageable);
        Page<ChatSessionDto> sessionDtos = sessions.map(ChatSessionDto::new);
        return sessionDtos;
    }

    private void startTimer(UUID sessionId, int duration) {
        final long[] remainingSeconds = {duration * 60L};

        ScheduledFuture<?> future = taskScheduler.scheduleAtFixedRate(() -> {
            if (remainingSeconds[0] <= 0) {
                // messagingTemplate.convertAndSend("/topic/chat/" + createdSession.getId() +
                // "/end", "Chat ended");
                endChat(sessionId);
                cancelTimer(sessionId); // Cancel timer
                return;
            }
            messagingTemplate.convertAndSend("/topic/chat/" + sessionId + "/timer", formatTime(remainingSeconds[0]));
            remainingSeconds[0] -= 1;

        }, Duration.ofSeconds(1));

        // Store the scheduled task so we can cancel it later
        timerTasks.put(sessionId, future);
    }

    private void cancelTimer(UUID sessionId) {
        ScheduledFuture<?> future = timerTasks.remove(sessionId);
        if (future != null && !future.isCancelled()) {
            future.cancel(false);
            logger.info("Timer cancelled for session {}", sessionId);
        }
    }

    private String formatTime(long totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
