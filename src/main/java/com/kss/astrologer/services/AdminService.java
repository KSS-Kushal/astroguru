package com.kss.astrologer.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kss.astrologer.dto.WalletDto;
import com.kss.astrologer.models.User;

@Service
public class AdminService {
    
    @Autowired
    private UserService userService;

    @Autowired
    private WalletService walletService;

    public WalletDto addBalanceInUserWallet(String mobile, double amount) {
        User user = userService.getUserByMobile(mobile);
        if (user == null) {
            throw new RuntimeException("User not found with mobile: " + mobile);
        }
        WalletDto wallet = walletService.creditBalance(user.getId(), amount, "Admin added balance");
        return wallet;
    }
}
