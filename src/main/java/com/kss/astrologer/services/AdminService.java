package com.kss.astrologer.services;

import com.kss.astrologer.dto.MonthlyProfitBarDto;
import com.kss.astrologer.dto.StatsResponseDto;
import com.kss.astrologer.exceptions.CustomException;
import com.kss.astrologer.models.Wallet;
import com.kss.astrologer.models.WalletTransaction;
import com.kss.astrologer.repository.UserRepository;
import com.kss.astrologer.repository.WalletTransactionRepository;
import com.kss.astrologer.types.MonthName;
import com.kss.astrologer.types.Role;
import com.kss.astrologer.types.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kss.astrologer.dto.WalletDto;
import com.kss.astrologer.models.User;

import java.util.*;

@Service
public class AdminService {
    
    @Autowired
    private UserService userService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletTransactionRepository transactionRepository;

    public WalletDto addBalanceInUserWallet(String mobile, double amount) {
        User user = userService.getUserByMobile(mobile);
        if (user == null) {
            throw new RuntimeException("User not found with mobile: " + mobile);
        }
        WalletDto wallet = walletService.creditBalance(user.getId(), amount, "Admin added balance");
        return wallet;
    }

    public UUID getAdminId() {
        List<User> users = userRepository.findByRole(Role.ADMIN);
        if (users.isEmpty()) throw new CustomException("Admin not found");
        User admin = users.get(0);
        return admin.getId();
    }
    public StatsResponseDto getStats() {
        Long totalUsers = userRepository.countByRole(Role.USER);
        Long totalAstrologers = userRepository.countByRole(Role.ASTROLOGER);

        Double userBalance = userRepository.getTotalWalletBalanceByRole(Role.USER);
        Double astrologerBalance = userRepository.getTotalWalletBalanceByRole(Role.ASTROLOGER);
        Double adminBalance = userRepository.getTotalWalletBalanceByRole(Role.ADMIN);

        return StatsResponseDto.builder()
                .totalUsers(totalUsers)
                .totalAstrologers(totalAstrologers)
                .totalUserWalletBalance(userBalance != null ? userBalance : 0.0)
                .totalAstrologerWalletBalance(astrologerBalance != null ? astrologerBalance : 0.0)
                .totalAdminWalletBalance(adminBalance != null ? adminBalance : 0.0)
                .build();
    }

    public List<MonthlyProfitBarDto> getAdminMonthlyProfitBarData(int year) {
        List<WalletTransaction> adminTxns = transactionRepository.findAllAdminTransactionsByYear(year);

        // Initialize all months to 0.0
        Map<Integer, Double> monthProfitMap = new HashMap<>();
        for (int i = 1; i <= 12; i++) {
            monthProfitMap.put(i, 0.0);
        }

        for (WalletTransaction txn : adminTxns) {
            int month = txn.getTimestamp().getMonthValue();
            double amount = txn.getAmount() != null ? txn.getAmount() : 0.0;

            if (txn.getType() == TransactionType.CREDIT) {
                monthProfitMap.put(month, monthProfitMap.get(month) + amount);
            } else if (txn.getType() == TransactionType.DEBIT) {
                monthProfitMap.put(month, monthProfitMap.get(month) - amount);
            }
        }

        List<MonthlyProfitBarDto> result = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            result.add(new MonthlyProfitBarDto(MonthName.fromInt(i), round(monthProfitMap.get(i))));
        }

        return result;
    }

    private double round(Double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
