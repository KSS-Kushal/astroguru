package com.kss.astrologer.services;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kss.astrologer.models.User;
import com.kss.astrologer.repository.UserRepository;
import com.kss.astrologer.types.Role;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    public User getUserByMobile(String mobile) {
        return userRepository.findByMobile(mobile).orElse(null);
    }

    public User getOrCreateUser(String mobile) {
        return userRepository.findByMobile(mobile).orElseGet(() ->
                userRepository.save(User.builder()
                        .mobile(mobile)
                        .role(Role.USER)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()));
    }
}
