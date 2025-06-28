package com.kss.astrologer.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.kss.astrologer.models.Wallet;
import com.kss.astrologer.models.WalletTransaction;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WalletDto {
    private UUID id;
    private UserDto user;
    private Double balance = 0.0;
    private List<WalletTransaction> transactions = new ArrayList<>();

    public WalletDto(Wallet wallet) {
        this.id = wallet.getId();
        this.user = new UserDto(wallet.getUser());
        this.balance = wallet.getBalance();
        this.transactions = wallet.getTransactions();
    }
}
