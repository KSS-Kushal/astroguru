package com.kss.astrologer.models;

import com.kss.astrologer.types.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallet_topups")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletTopup {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID userId;

    @OneToOne
    private WalletTransaction walletTransaction;

    private Double amount;

    private String orderId;

    private String paymentId;

    private PaymentStatus status; // CREATED, SUCCESS, FAILED, PENDING

    private Boolean isFirstTopUp = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();
}
