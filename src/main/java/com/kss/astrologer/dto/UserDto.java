package com.kss.astrologer.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.kss.astrologer.models.User;
import com.kss.astrologer.types.Role;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDto {
    private UUID id;
    private String name;
    private String mobile;
    private Role role;
    private Double walletBalance;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.mobile = user.getMobile();
        this.role = user.getRole();
        this.walletBalance = user.getWallet() != null ? user.getWallet().getBalance() : 0.0;
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }
}
