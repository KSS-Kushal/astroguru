package com.kss.astrologer.services;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.kss.astrologer.dto.ChatQueueEntry;
import com.kss.astrologer.models.ChatSession;
import com.kss.astrologer.repository.ChatSessionRepository;
import com.kss.astrologer.types.ChatStatus;

@Service
public class ChatQueueService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    private String getQueueKey(UUID astrologerId) {
        return "chat_queue:" + astrologerId;
    }

    private String formatEntry(UUID userId, int minutes) {
        return userId + ":" + minutes;
    }

    public UUID parseUserId(String entry) {
        return UUID.fromString(entry.split(":")[0]);
    }

    public int parseRequestedMinutes(String entry) {
        return Integer.parseInt(entry.split(":")[1]);
    }

    public void enqueue(UUID astrologerId, UUID userId, int requestedMinutes) {
        redisTemplate.opsForList().rightPush(getQueueKey(astrologerId), formatEntry(userId, requestedMinutes));
    }

    public String dequeue(UUID astrologerId) {
        return redisTemplate.opsForList().leftPop(getQueueKey(astrologerId));
    }

    public List<ChatQueueEntry> getQueue(UUID astrologerId) {
        List<String> rawEntries = redisTemplate.opsForList().range(getQueueKey(astrologerId), 0, -1);
        if (rawEntries == null) return new ArrayList<>();

        return rawEntries.stream()
                .map(entry -> new ChatQueueEntry(parseUserId(entry), parseRequestedMinutes(entry)))
                .collect(Collectors.toList());
    }

    public long getPosition(UUID astrologerId, UUID userId) {
        List<ChatQueueEntry> queue = getQueue(astrologerId);
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).getUserId().equals(userId)) {
                return i;
            }
        }
        return -1;
    }

    public void removeUser(UUID astrologerId, UUID userId) {
        List<String> queue = redisTemplate.opsForList().range(getQueueKey(astrologerId), 0, -1);
        if (queue == null) return;

        for (String entry : queue) {
            if (parseUserId(entry).equals(userId)) {
                redisTemplate.opsForList().remove(getQueueKey(astrologerId), 1, entry);
                break;
            }
        }
    }

    public long getQueueLength(UUID astrologerId) {
        return redisTemplate.opsForList().size(getQueueKey(astrologerId));
    }

    public long estimateWaitingTime(UUID astrologerId, UUID requestingUserId) {
        long waitingTime = 0;

        Optional<ChatSession> activeSession = chatSessionRepository.findByAstrologerIdAndStatus(astrologerId, ChatStatus.ACTIVE);
        if (activeSession.isPresent()) {
            ChatSession session = activeSession.get();
            long elapsed = Duration.between(session.getStartedAt(), LocalDateTime.now()).toMinutes();
            long remaining = session.getTotalMinutes() - elapsed;
            waitingTime += Math.max(remaining, 0);
        }

        List<ChatQueueEntry> queue = getQueue(astrologerId);
        for (ChatQueueEntry entry : queue) {
            if (entry.getUserId().equals(requestingUserId)) break;
            waitingTime += entry.getRequestedMinutes();
        }

        return waitingTime;
    }

    public boolean isNextInQueue(UUID astrologerId, UUID userId) {
        List<ChatQueueEntry> queue = getQueue(astrologerId);
        if (queue.isEmpty()) return false;
        return queue.get(0).getUserId().equals(userId);
    }

}
