package com.kss.astrologer.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kss.astrologer.models.Wallet;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    
}
