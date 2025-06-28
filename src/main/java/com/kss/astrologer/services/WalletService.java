package com.kss.astrologer.services;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kss.astrologer.dto.WalletDto;
import com.kss.astrologer.models.Wallet;
import com.kss.astrologer.models.WalletTransaction;
import com.kss.astrologer.repository.WalletRepository;
import com.kss.astrologer.repository.WalletTransactionRepository;
import com.kss.astrologer.types.TransactionType;

@Service
public class WalletService {
    
    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    public Wallet getWalletByUserId(UUID userId) {
        return walletRepository.findByUserId(userId).orElse(null);
    }

    @Transactional
    public WalletDto creditBalance(UUID userId, double amount, String description) {
        Wallet wallet = walletRepository.findByUserId(userId).orElse(null);
        if (wallet == null) {
            throw new RuntimeException("Wallet not found for user ID: " + userId);
        }
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(wallet);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setType(TransactionType.CREDIT);

        transaction = walletTransactionRepository.save(transaction);

        wallet.setBalance(wallet.getBalance() + amount);
        List<WalletTransaction> walletTransactions = wallet.getTransactions();
        walletTransactions.add(transaction);
        System.out.println("transactions : " + walletTransactions.size());
        wallet.setTransactions(walletTransactions);
        wallet = walletRepository.save(wallet);
        
        return new WalletDto(wallet);
    }

    @Transactional
    public WalletDto debitBalance(UUID userId, double amount, String description) {
        Wallet wallet = walletRepository.findByUserId(userId).orElse(null);
        if (wallet == null) {
            throw new RuntimeException("Wallet not found for user ID: " + userId);
        }
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(wallet);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setType(TransactionType.DEBIT);

        transaction = walletTransactionRepository.save(transaction);

        wallet.setBalance(wallet.getBalance() - amount);
        List<WalletTransaction> walletTransactions = wallet.getTransactions();
        walletTransactions.add(transaction);
        wallet.setTransactions(walletTransactions);
        wallet = walletRepository.save(wallet);
        
        return new WalletDto(wallet);
    }
}
