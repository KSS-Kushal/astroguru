package com.kss.astrologer.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "withdraws")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Withdraw {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private User user;

    @OneToOne(cascade = CascadeType.ALL)
    private WalletTransaction walletTransaction;

    private Double amount;
    private Boolean isApproved = false;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime approvedAt;
}
