package com.kss.astrologer.services;

import java.util.Set;
import java.util.UUID;

import com.kss.astrologer.models.User;
import com.kss.astrologer.types.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class OnlineUserService {
    private final String ONLINE_USERS_KEY = "online_users";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserService userService;

    public void addUser(UUID userId) {
        User user = userService.getById(userId);
        if(user.getRole() != Role.ASTROLOGER) return;
        redisTemplate.opsForSet().add(ONLINE_USERS_KEY, userId.toString());
    }

    public void removeUser(UUID userId) {
        User user = userService.getById(userId);
        if(user.getRole() != Role.ASTROLOGER) return;
        redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, userId.toString());
    }

    public boolean isOnline(UUID userId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(ONLINE_USERS_KEY, userId.toString()));
    }

    public Set<String> getAllOnlineUsers() {
        return redisTemplate.opsForSet().members(ONLINE_USERS_KEY);
    }

    public void sendNotification(UUID userId) {
        User user = userService.getById(userId);
        if(user.getRole() != Role.ASTROLOGER) return;
        Set<String> getAllOnlineAstrologers = getAllOnlineUsers();
        messagingTemplate.convertAndSend("/topic/online/astrologer", getAllOnlineAstrologers);
    }
}
