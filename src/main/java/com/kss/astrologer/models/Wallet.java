package com.kss.astrologer.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wallets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    private Double balance = 0.0;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL)
    private List<WalletTransaction> transactions = new ArrayList<>();
}

