package com.kss.astrologer.services;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.kss.astrologer.dto.UserDto;
import com.kss.astrologer.exceptions.CustomException;
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

    public User getOrCreateAdmin(String mobile) {
        return userRepository.findByMobile(mobile).orElseGet(() ->
                userRepository.save(User.builder()
                        .mobile(mobile)
                        .role(Role.ADMIN)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()));
    }
    
    public UserDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "User not found"));
        return new UserDto(user);
    }
}
