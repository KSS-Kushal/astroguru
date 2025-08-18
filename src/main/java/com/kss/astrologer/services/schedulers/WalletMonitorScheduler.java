package com.kss.astrologer.services.schedulers;

import com.kss.astrologer.models.Wallet;
import com.kss.astrologer.models.WalletTransaction;
import com.kss.astrologer.models.Withdraw;
import com.kss.astrologer.repository.WalletRepository;
import com.kss.astrologer.repository.WithdrawRepository;
import com.kss.astrologer.types.TransactionType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletMonitorScheduler {
    private static final Logger logger = LoggerFactory.getLogger(WalletMonitorScheduler.class);
    private final WalletRepository walletRepository;
    private final WithdrawRepository withdrawRepository;

    @Scheduled(cron = "0 30 0 * * *") // Runs daily at 12:30 AM
    @Transactional
    public void updateWalletBalances() {
        List<Wallet> wallets = walletRepository.findAll();

        for (Wallet wallet : wallets) {
            UUID userId = wallet.getUser().getId();

            // Sum all wallet transactions
            double credit = wallet.getTransactions().stream()
                    .filter(t -> t.getType() == TransactionType.CREDIT)
                    .mapToDouble(WalletTransaction::getAmount)
                    .sum();

            double debit = wallet.getTransactions().stream()
                    .filter(t -> t.getType() == TransactionType.DEBIT)
                    .mapToDouble(WalletTransaction::getAmount)
                    .sum();

            // Include pending withdraws as debit
            double pendingWithdraws = withdrawRepository
                    .findByUserIdAndIsApprovedFalse(userId)
                    .stream()
                    .mapToDouble(Withdraw::getAmount)
                    .sum();

            double balance = credit - (debit + pendingWithdraws);
            wallet.setBalance(balance);

            walletRepository.save(wallet);
        }

        logger.info("Wallet balances updated at: {}", LocalDateTime.now());
    }
}
