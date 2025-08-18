package com.kss.astrologer.services;

import com.kss.astrologer.dto.WithdrawDto;
import com.kss.astrologer.exceptions.CustomException;
import com.kss.astrologer.models.User;
import com.kss.astrologer.models.Wallet;
import com.kss.astrologer.models.WalletTransaction;
import com.kss.astrologer.models.Withdraw;
import com.kss.astrologer.repository.WalletRepository;
import com.kss.astrologer.repository.WalletTransactionRepository;
import com.kss.astrologer.repository.WithdrawRepository;
import com.kss.astrologer.types.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class WithdrawService {

    @Autowired
    private WithdrawRepository withdrawRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Transactional
    public WithdrawDto createWithdrawRequest(Double amount, UUID userId) {
        if(amount == null || amount<=0) throw new CustomException("Amount must be greater than 0.0");
        User user = userService.getById(userId);
        Wallet wallet = user.getWallet();
        if(wallet.getBalance()<amount) throw new CustomException("Insufficient balance");

        WalletTransaction transaction = new WalletTransaction();
        transaction.setAmount(amount);
        transaction.setDescription("Balance Withdraw");
        transaction.setWallet(wallet);
        transaction.setType(TransactionType.PENDING);
        transaction.setTimestamp(LocalDateTime.now());
        transaction = walletTransactionRepository.save(transaction);

        wallet.setBalance(wallet.getBalance() - amount);
        List<WalletTransaction> transactions = wallet.getTransactions();
        transactions.add(transaction);
        wallet.setTransactions(transactions);
        walletRepository.save(wallet);

        Withdraw withdraw = new Withdraw();
        withdraw.setUser(user);
        withdraw.setAmount(amount);
        withdraw.setWalletTransaction(transaction);
        withdraw.setIsApproved(false);
        withdraw = withdrawRepository.save(withdraw);

        return new WithdrawDto(withdraw);
    }

    @Transactional
    public WithdrawDto approvedWithdrawRequest(UUID id) {
        Withdraw withdraw = withdrawRepository.findById(id).orElseThrow(
                () -> new CustomException(HttpStatus.NOT_FOUND, "Withdraw request not found"));
        withdraw.setIsApproved(true);
        withdraw.setApprovedAt(LocalDateTime.now());
        withdraw = withdrawRepository.save(withdraw);

        WalletTransaction transaction = withdraw.getWalletTransaction();
        transaction.setType(TransactionType.DEBIT);
        walletTransactionRepository.save(transaction);

        return new WithdrawDto(withdraw);
    }

    public Page<WithdrawDto> getWithdrawRequest(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt"));
        Page<Withdraw> withdrawRequests = withdrawRepository.findByIsApproved(false, pageable);
        return withdrawRequests.map(WithdrawDto::new);
    }
}
