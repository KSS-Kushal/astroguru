package com.kss.astrologer.services;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class OnlineUserService {
    private final String ONLINE_USERS_KEY = "online_users";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void addUser(UUID userId) {
        redisTemplate.opsForSet().add(ONLINE_USERS_KEY, userId.toString());
    }

    public void removeUser(UUID userId) {
        redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, userId.toString());
    }

    public boolean isOnline(UUID userId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(ONLINE_USERS_KEY, userId.toString()));
    }

    public Set<String> getAllOnlineUsers() {
        return redisTemplate.opsForSet().members(ONLINE_USERS_KEY);
    }
}
