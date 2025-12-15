package com.kss.astrologer.services;

import java.util.List;
import java.util.UUID;

import com.kss.astrologer.models.User;
import com.kss.astrologer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kss.astrologer.dto.WalletDto;
import com.kss.astrologer.exceptions.CustomException;
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
    private UserRepository userRepository;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    public Wallet getWalletByUserId(UUID userId) {
        return walletRepository.findByUserId(userId).orElse(null);
    }

    @Transactional
    public WalletDto creditBalance(UUID userId, double amount, String description) {
        Wallet wallet = walletRepository.findByUserId(userId).orElse(null);
        if (wallet == null) {
            throw new CustomException("Wallet not found for user ID: " + userId);
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
            throw new CustomException("Wallet not found for user ID: " + userId);
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

    public Wallet addLockedBalance(UUID walletId, Double amount) {
        if (amount == null) throw new CustomException("Amount can't be null");
        Wallet wallet = walletRepository.findById(walletId).orElseThrow(() -> new CustomException("Wallet not found"));
        double lockedBalance = wallet.getLockedBalance() != null ? wallet.getLockedBalance() : 0.0;
        wallet.setLockedBalance(lockedBalance + Math.abs(amount));
        return walletRepository.save(wallet);
    }

    public Wallet subtractLockedBalance(UUID walletId, Double amount) {
        if (amount == null) throw new CustomException("Amount can't be null");
        Wallet wallet = walletRepository.findById(walletId).orElseThrow(() -> new CustomException("Wallet not found"));
        double lockedBalance = wallet.getLockedBalance() != null ? wallet.getLockedBalance() : 0.0;
        if(Math.abs(amount)>lockedBalance) throw new CustomException("Insufficient Balance");
        wallet.setLockedBalance(lockedBalance - Math.abs(amount));
        return walletRepository.save(wallet);
    }

    public Page<WalletTransaction> getTransaction(UUID walletId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Direction.DESC, "timestamp");
        return walletTransactionRepository.findByWalletId(walletId, pageable);
    }

    public WalletTransaction topup(UUID userId, double amount) {
        Wallet wallet = walletRepository.findByUserId(userId).orElse(null);
        if (wallet == null) {
            throw new CustomException("Wallet not found for user ID: " + userId);
        }
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(wallet);
        transaction.setAmount(amount);
        transaction.setDescription("Wallet TopUp");
        transaction.setType(TransactionType.PENDING);

        transaction = walletTransactionRepository.save(transaction);

        return transaction;
    }

    @Transactional
    public WalletTransaction topup(WalletTransaction transaction, double amount, TransactionType type, boolean isFirstTopUp) {
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction = walletTransactionRepository.save(transaction);

        Wallet wallet = transaction.getWallet();
        WalletDto walletDto = new WalletDto(wallet);

        if (type == TransactionType.CREDIT) {
            if(isFirstTopUp) {
                walletDto = creditBalance(wallet.getUser().getId(), transaction.getAmount() * 0.5, "TopUp Cashback");
                User user = wallet.getUser();
                user.setIsFirstTopUpDone(true);
                userRepository.save(user);
            }
            wallet.setBalance(walletDto.getBalance() + transaction.getAmount());
            List<WalletTransaction> walletTransactions = walletDto.getTransactions();
            walletTransactions.add(transaction);
            wallet.setTransactions(walletTransactions);
            walletRepository.save(wallet);
        }
        return transaction;
    }
}
