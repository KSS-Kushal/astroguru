package com.kss.astrologer.services;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kss.astrologer.dto.ChatQueueEntry;
import com.kss.astrologer.exceptions.CustomException;
import com.kss.astrologer.models.ChatSession;
import com.kss.astrologer.repository.ChatSessionRepository;
import com.kss.astrologer.types.ChatStatus;

@Service
public class ChatQueueService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    private String getQueueKey(UUID astrologerId) {
        return "chat_queue:" + astrologerId;
    }

    public void enqueue(UUID astrologerId, UUID userId, int requestedMinutes) {
        ChatQueueEntry entry = new ChatQueueEntry(userId, requestedMinutes);
        try {
            String json = objectMapper.writeValueAsString(entry);
            redisTemplate.opsForList().rightPush(getQueueKey(astrologerId), json);
        } catch (Exception e) {
            throw new CustomException("Failed to serialize queue entry");
        }
    }

    public String dequeue(UUID astrologerId) {
        return redisTemplate.opsForList().leftPop(getQueueKey(astrologerId));
    }

    public List<ChatQueueEntry> getQueue(UUID astrologerId) {
        List<String> entries = redisTemplate.opsForList().range(getQueueKey(astrologerId), 0, -1);
        if (entries == null)
            return List.of();

        return entries.stream()
                .map(json -> {
                    try {
                        return objectMapper.readValue(json, ChatQueueEntry.class);
                    } catch (Exception e) {
                        throw new CustomException("Failed to deserialize queue entry");
                    }
                })
                .toList();
    }

    public long getPosition(UUID astrologerId, UUID userId) {
        List<String> rawQueue = redisTemplate.opsForList().range(getQueueKey(astrologerId), 0, -1);
        if (rawQueue == null)
            return -1;

        for (int i = 0; i < rawQueue.size(); i++) {
            try {
                ChatQueueEntry entry = objectMapper.readValue(rawQueue.get(i), ChatQueueEntry.class);
                if (entry.getUserId().equals(userId)) {
                    return i;
                }
            } catch (Exception e) {
                throw new CustomException("Failed to deserialize queue entry at position " + i);
            }
        }

        return -1; // Not found
    }

    public void removeUser(UUID astrologerId, UUID userId) {
        String key = getQueueKey(astrologerId);
        List<String> rawQueue = redisTemplate.opsForList().range(key, 0, -1);
        if (rawQueue == null)
            return;

        for (String item : rawQueue) {
            try {
                ChatQueueEntry entry = objectMapper.readValue(item, ChatQueueEntry.class);
                if (entry.getUserId().equals(userId)) {
                    redisTemplate.opsForList().remove(key, 1, item); // remove one match
                    break;
                }
            } catch (Exception e) {
                throw new CustomException("Failed to deserialize queue entry " + item);
            }
        }
    }

    public long getQueueLength(UUID astrologerId) {
        return redisTemplate.opsForList().size(getQueueKey(astrologerId));
    }

    public long estimateWaitingTime(UUID astrologerId, UUID requestingUserId) {
        long waitingTime = 0;

        // 1. Remaining time from current session
        Optional<ChatSession> activeSession = chatSessionRepository.findByAstrologerIdAndStatus(astrologerId,
                ChatStatus.ACTIVE);
        if (activeSession.isPresent()) {
            ChatSession session = activeSession.get();
            long elapsed = Duration.between(session.getStartedAt(), LocalDateTime.now()).toMinutes();
            long remaining = session.getTotalMinutes() - elapsed;
            waitingTime += Math.max(remaining, 0);
        }

        // 2. Sum requestedMinutes for all users before the current one
        List<ChatQueueEntry> queue = getQueue(astrologerId);
        for (ChatQueueEntry entry : queue) {
            if (entry.getUserId().equals(requestingUserId))
                break;
            waitingTime += entry.getRequestedMinutes();
        }

        return waitingTime;
    }

}
