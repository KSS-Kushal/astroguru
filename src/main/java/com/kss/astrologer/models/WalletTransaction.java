package com.kss.astrologer.models;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.kss.astrologer.types.TransactionType;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wallet_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "wallet_id")
    @JsonBackReference
    private Wallet wallet;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type; // CREDIT, DEBIT

    private String description;

    private LocalDateTime timestamp = LocalDateTime.now();
}

