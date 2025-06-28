package com.kss.astrologer.services;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kss.astrologer.dto.UserDto;
import com.kss.astrologer.exceptions.CustomException;
import com.kss.astrologer.models.User;
import com.kss.astrologer.models.Wallet;
import com.kss.astrologer.repository.UserRepository;
import com.kss.astrologer.repository.WalletRepository;
import com.kss.astrologer.request.KundliRequest;
import com.kss.astrologer.types.Role;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    public User getUserByMobile(String mobile) {
        return userRepository.findByMobile(mobile).orElse(null);
    }

    @Transactional
    public User getOrCreateUser(String mobile) {
        return userRepository.findByMobile(mobile).orElseGet(() ->
                {
                    Wallet wallet = new Wallet();
                    wallet.setBalance(0.0);
                    wallet = walletRepository.save(wallet);
                    User user = userRepository.save(User.builder()
                        .mobile(mobile)
                        .role(Role.USER)
                        .wallet(wallet)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build());
                    wallet.setUser(user);
                    walletRepository.save(wallet);
                    return user;
                });
    }

    @Transactional
    public User getOrCreateAdmin(String mobile) {
        return userRepository.findByMobile(mobile).orElseGet(() ->
                {
                    Wallet wallet = new Wallet();
                    wallet.setBalance(0.0);
                    wallet = walletRepository.save(wallet);
                    User user = userRepository.save(User.builder()
                        .mobile(mobile)
                        .role(Role.ADMIN)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build());
                    wallet.setUser(user);
                    walletRepository.save(wallet);
                    return user;
                });
    }
    
    public UserDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "User not found"));
        return new UserDto(user);
    }

    public UserDto addUserDetails(KundliRequest data, UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "User not found"));

        user.setName(data.getName());
        user.setGender(data.getGender());
        user.setBirthDate(data.getBirthDate());
        user.setBirthTime(data.getBirthTime());
        user.setBirthPlace(data.getBirthPlace());
        user.setLatitude(data.getLatitude());
        user.setLongitude(data.getLongitude());
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        return new UserDto(updatedUser);
    }

    public User getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
