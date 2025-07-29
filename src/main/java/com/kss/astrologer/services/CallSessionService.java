package com.kss.astrologer.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kss.astrologer.dto.CallSessionDto;
import com.kss.astrologer.dto.ChatQueueEntry;
import com.kss.astrologer.dto.QueueNotificationDto;
import com.kss.astrologer.exceptions.CustomException;
import com.kss.astrologer.models.*;
import com.kss.astrologer.repository.AstrologerRepository;
import com.kss.astrologer.repository.CallSessionRepository;
import com.kss.astrologer.repository.UserRepository;
import com.kss.astrologer.repository.WalletRepository;
import com.kss.astrologer.types.ChatStatus;
import com.kss.astrologer.types.SessionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
public class CallSessionService {

    private static final Logger logger = LoggerFactory.getLogger(CallSessionService.class);

    @Autowired
    private ChatQueueService queueService;

    @Autowired
    private CallSessionRepository callSessionRepo;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AstrologerRepository astrologerRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskScheduler taskScheduler;

    private final Map<UUID, ScheduledFuture<?>> timerTasks = new ConcurrentHashMap<>();

    public long requestCall(UUID userId, UUID astrologerId, int requestedMinutes, SessionType type) {
        logger.info("Duration: " + requestedMinutes);
        AstrologerDetails astrologer = astrologerRepository.findByUserId(astrologerId)
                .orElseThrow(() -> new CustomException("Astrologer not found"));

        Double perMinuteRate = type == SessionType.AUDIO ? astrologer.getPricePerMinuteVoice() : astrologer.getPricePerMinuteVideo();
        Wallet wallet = walletService.getWalletByUserId(userId);
        if (wallet == null) {
            throw new CustomException("Wallet not found for user ID: " + userId);
        }
        Double totalCharge = perMinuteRate * requestedMinutes;

        if (wallet.getBalance().compareTo(totalCharge) < 0) {
            throw new CustomException("Not enough balance for " + requestedMinutes + " minutes.");
        }
        queueService.enqueue(astrologerId, userId, requestedMinutes, type);
        long pos = queueService.getPosition(astrologerId, userId);
        QueueNotificationDto queueNotificationDto = new QueueNotificationDto(userId, type, "New " + type.name() + " call request received");
        messagingTemplate.convertAndSend("/topic/queue/" + astrologerId, queueNotificationDto);
        return pos + 1;
    }

    public String acceptCall(UUID userId, UUID astrologerId) {
        if (!queueService.isNextInQueue(astrologerId, userId)) {
            throw new CustomException("User is not first in the queue.");
        }

        String entry = queueService.dequeue(astrologerId);
        ChatQueueEntry queueEntry = queueService.parseEntry(entry);
        if (queueEntry.getSessionType() != SessionType.AUDIO && queueEntry.getSessionType() != SessionType.VIDEO) {
            throw new CustomException("Not a call session.");
        }

        startCall(userId, astrologerId, queueEntry.getRequestedMinutes(), queueEntry.getSessionType());
        return "Call accepted and started.";
    }

    @Transactional
    public void startCall(UUID userId, UUID astrologerId, int requestedMinutes, SessionType type) {
//        if (requestedMinutes < 5) {
//            throw new CustomException("Minimum chat duration is 5 minutes.");
//        }

        Optional<CallSession> existing = callSessionRepo.findByAstrologerIdAndStatus(astrologerId, ChatStatus.ACTIVE);
        if (existing.isPresent()) {
            logger.warn("Call already active for astrologer: {}", astrologerId);
            CallSessionDto existingSessionDto = new CallSessionDto(existing.get());

            System.out.println(existingSessionDto);
            // Send session info to frontend
            if(existingSessionDto.getUser().getId() == userId && existingSessionDto.getAstrologer().getId() == astrologerId) {
                messagingTemplate.convertAndSend("/topic/call/" + userId + "/session", existingSessionDto);
                messagingTemplate.convertAndSend("/topic/call/" + astrologerId + "/session", existingSessionDto);
            }
            return; // Prevent duplicate start
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "User not found"));
        AstrologerDetails astrologer = astrologerRepository.findByUserId(astrologerId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Astrologer not found"));

        Double perMinuteRate = type == SessionType.AUDIO ? astrologer.getPricePerMinuteVoice() : astrologer.getPricePerMinuteVideo();
        Double totalCharge = perMinuteRate * requestedMinutes;

        Wallet wallet = user.getWallet();

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

        // Deduct wallet amount from user
        walletService.debitBalance(userId, totalCharge, type.name() + " Call session with astrologer " + astrologer.getUser().getName() + " for "
                + requestedMinutes + " minutes.");

        // Credit amount to astrologer's wallet
        walletService.creditBalance(astrologerId, totalCharge, type.name() + " Call session with user " + user.getName() + " for " + requestedMinutes + " minutes.");

        String channelName = UUID.randomUUID().toString();
//        String token = agoraService.generateToken(channelName, userId.toString(), requestedMinutes);

        CallSession session = CallSession.builder()
                .user(user)
                .astrologer(astrologer.getUser())
                .startedAt(LocalDateTime.now())
                .status(ChatStatus.ACTIVE)
                .totalMinutes(requestedMinutes)
                .totalCost(totalCharge)
                .sessionType(type)
                .agoraChannelName(channelName)
//                .agoraToken(token)
                .build();

        CallSession createdSession = callSessionRepo.save(session);
        CallSessionDto createdSessionDto = new CallSessionDto(createdSession);

        System.out.println(createdSessionDto);
        // Send session info to frontend
        messagingTemplate.convertAndSend("/topic/call/" + userId + "/session", createdSessionDto);
        messagingTemplate.convertAndSend("/topic/call/" + astrologerId + "/session", createdSessionDto);

        startTimer(createdSession.getId(), createdSession.getTotalMinutes());
    }

    public void endCall(UUID sessionId) {
        CallSession session = callSessionRepo.findById(sessionId)
                .orElseThrow(() -> new CustomException("Session not found"));
        session.setEndedAt(LocalDateTime.now());
        session.setStatus(ChatStatus.ENDED);

        callSessionRepo.save(session);

        try {
            messagingTemplate.convertAndSend("/topic/call/" + session.getId(),
                    objectMapper.writeValueAsString(Map.of("status", "ended")));
        } catch (Exception e) {
            logger.error("Error to send msg end notification: ", e);
        }

        // Trigger next user from queue
        UUID astrologerId = session.getAstrologer().getId();
        String nextUser = queueService.peek(astrologerId);
        if (nextUser != null) {
            ChatQueueEntry queueEntry = queueService.parseEntry(nextUser);
            QueueNotificationDto queueNotificationDto = new QueueNotificationDto(queueEntry.getUserId(), queueEntry.getSessionType(), "Call ended, next user can be served");
            messagingTemplate.convertAndSend("/topic/queue/" + astrologerId, queueNotificationDto);
        }
    }

    public Page<CallSessionDto> getHistory(UUID userId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page-1, size, Sort.Direction.DESC, "startedAt");
        Page<CallSession> sessions = callSessionRepo.findByUserIdOrAstrologerId(userId, userId, pageable);
        Page<CallSessionDto> sessionDtos = sessions.map(CallSessionDto::new);
        return sessionDtos;
    }

    public CallSessionDto getActiveSession(UUID astrologerId) {
        Optional<CallSession> session = callSessionRepo.findByAstrologerIdAndStatus(astrologerId, ChatStatus.ACTIVE);
        return session.map(CallSessionDto::new).orElse(null);
    }

    private void startTimer(UUID sessionId, int duration) {
        final long[] remainingSeconds = {duration * 60L};

        logger.info("SessionId: " + sessionId);
        ScheduledFuture<?> future = null;
        try {
            future = taskScheduler.scheduleAtFixedRate(() -> {
                if (remainingSeconds[0] <= 0) {
                    endCall(sessionId);
                    cancelTimer(sessionId); // Cancel timer
                    return;
                }
                logger.info("timer: " + formatTime(remainingSeconds[0]));
                messagingTemplate.convertAndSend("/topic/call/" + sessionId + "/timer", formatTime(remainingSeconds[0]));
                remainingSeconds[0] -= 1;

            }, Duration.ofSeconds(1));
        } catch (Exception e) {
            logger.error("Error in Call timer: ", e);
            if (future != null && !future.isCancelled()) {
                future.cancel(false);
            }
        }

        // Store the scheduled task so we can cancel it later
        timerTasks.put(sessionId, future);
    }

    public void endCallByUser(UUID sessionId) {
        CallSession session = callSessionRepo.findById(sessionId).orElse(null);
        if(session == null) return;
        endCall(sessionId);
        cancelTimer(sessionId); // Cancel timer
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
